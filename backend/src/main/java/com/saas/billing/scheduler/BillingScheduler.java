package com.saas.billing.scheduler;

import com.saas.billing.entity.*;
import com.saas.billing.repository.*;
import com.saas.billing.service.InvoiceService;
import com.saas.billing.service.NotificationService;
import com.saas.billing.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRetryRepository paymentRetryRepository;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final AuditLogRepository auditLogRepository;
    private final UsageLogRepository usageLogRepository;

    /**
     * Daily subscription renewal execution.
     * Evaluates active subscriptions that have ended their period and renews them.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    @Transactional
    public void renewSubscriptions() {
        log.info("Starting subscription renewal sweep...");
        OffsetDateTime now = OffsetDateTime.now();
        List<Subscription> expiredSubs = subscriptionRepository.findExpiredSubscriptions(now);

        for (Subscription sub : expiredSubs) {
            try {
                if (sub.getCancelAtPeriodEnd()) {
                    sub.setStatus("EXPIRED");
                    sub.setEndedAt(now);
                    subscriptionRepository.save(sub);

                    log.info("Subscription for organization {} has expired according to cancellation flag.", sub.getOrganization().getName());

                    // Record history
                    subscriptionHistoryRepository.save(SubscriptionHistory.builder()
                            .subscription(sub)
                            .organization(sub.getOrganization())
                            .plan(sub.getPlan())
                            .action("EXPIRE")
                            .statusBefore("ACTIVE")
                            .statusAfter("EXPIRED")
                            .notes("Subscription cancelled and expired at period end")
                            .build());

                    notificationService.createInAppNotification(
                            sub.getOrganization().getId(),
                            null,
                            "Subscription Expired",
                            "Your subscription tier has expired. Please upgrade to restore full access."
                    );
                } else {
                    // Renew subscription and generate new invoice
                    OffsetDateTime oldEnd = sub.getCurrentPeriodEnd();
                    sub.setCurrentPeriodStart(oldEnd);
                    sub.setCurrentPeriodEnd(oldEnd.plusMonths(sub.getPlan().getBillingInterval().equalsIgnoreCase("yearly") ? 12 : 1));
                    subscriptionRepository.save(sub);

                    log.info("Auto-renewing subscription for organization: {}", sub.getOrganization().getName());

                    // Generate Invoice
                    Invoice invoice = Invoice.builder()
                            .organization(sub.getOrganization())
                            .subscription(sub)
                            .invoiceNumber("INV-REN-" + System.currentTimeMillis())
                            .amountDue(sub.getPlan().getAmount())
                            .subtotal(sub.getPlan().getAmount())
                            .total(sub.getPlan().getAmount())
                            .status("OPEN")
                            .dueDate(now.plusDays(7))
                            .billingReason("SUBSCRIPTION_CYCLE")
                            .currency(sub.getPlan().getCurrency())
                            .build();

                    invoiceRepository.save(invoice);

                    // Dispatch notification
                    notificationService.createInAppNotification(
                            sub.getOrganization().getId(),
                            null,
                            "Subscription Renewed",
                            "Your subscription has been renewed for the next billing cycle. Invoice number: " + invoice.getInvoiceNumber()
                    );
                }
            } catch (Exception e) {
                log.error("Failed to renew subscription {}: {}", sub.getId(), e.getMessage());
            }
        }
    }

    /**
     * Daily trial expiration sweep.
     * Transitions ended trials to EXPIRED state.
     */
    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1:00 AM
    @Transactional
    public void expireTrials() {
        log.info("Starting free trial expiration sweep...");
        OffsetDateTime now = OffsetDateTime.now();
        List<Subscription> expiredTrials = subscriptionRepository.findExpiredTrials(now);

        for (Subscription sub : expiredTrials) {
            try {
                sub.setStatus("EXPIRED");
                sub.setEndedAt(now);
                subscriptionRepository.save(sub);

                subscriptionHistoryRepository.save(SubscriptionHistory.builder()
                        .subscription(sub)
                        .organization(sub.getOrganization())
                        .plan(sub.getPlan())
                        .action("EXPIRE")
                        .statusBefore("TRIAL")
                        .statusAfter("EXPIRED")
                        .notes("Trial period has expired")
                        .build());

                notificationService.createInAppNotification(
                        sub.getOrganization().getId(),
                        null,
                        "Free Trial Ended",
                        "Your 14-day free trial has ended. Subscribe to a plan to keep using features."
                );
                log.info("Expired free trial for organization: {}", sub.getOrganization().getName());
            } catch (Exception e) {
                log.error("Failed to expire trial sub {}: {}", sub.getId(), e.getMessage());
            }
        }
    }

    /**
     * Automated Payment Retry Execution.
     * Periodically checks open invoices with failed retries and triggers charge attempts.
     */
    @Scheduled(cron = "0 0/15 * * * ?") // Every 15 minutes
    @Transactional
    public void retryFailedPayments() {
        log.info("Sweeping for scheduled payment retries...");
        OffsetDateTime now = OffsetDateTime.now();
        List<PaymentRetry> pendingRetries = paymentRetryRepository.findPendingRetriesBefore(now);

        for (PaymentRetry retry : pendingRetries) {
            Invoice inv = retry.getInvoice();
            try {
                log.info("Retrying payment for invoice: {}. Attempt count: {}", inv.getInvoiceNumber(), retry.getRetryCount() + 1);

                // Simulate payment request
                paymentService.retryPayment(inv.getId());

                retry.setStatus("COMPLETED");
                paymentRetryRepository.save(retry);

                notificationService.createInAppNotification(
                        inv.getOrganization().getId(),
                        null,
                        "Payment Succeeded",
                        "Automatic payment retry for invoice " + inv.getInvoiceNumber() + " succeeded."
                );
            } catch (Exception e) {
                int count = retry.getRetryCount() + 1;
                retry.setRetryCount(count);

                if (count >= 3) {
                    retry.setStatus("FAILED_FINAL");
                    retry.setNextRetryAt(null);
                    paymentRetryRepository.save(retry);

                    // Update invoice status
                    inv.setStatus("UNCOLLECTIBLE");
                    invoiceRepository.save(inv);

                    // Set subscription status to FAILED_PAYMENT (blocking tenant access)
                    if (inv.getSubscription() != null) {
                        Subscription sub = inv.getSubscription();
                        sub.setStatus("FAILED_PAYMENT");
                        subscriptionRepository.save(sub);
                    }

                    notificationService.createInAppNotification(
                            inv.getOrganization().getId(),
                            null,
                            "Payment Failed - Workspace Locked",
                            "Automatic payment retries have failed for invoice " + inv.getInvoiceNumber() + ". Your account is locked."
                    );
                    log.error("Payment retry failed final for invoice: {}", inv.getInvoiceNumber());
                } else {
                    retry.setNextRetryAt(now.plusDays(1)); // Schedule next retry in 1 day
                    paymentRetryRepository.save(retry);
                    log.warn("Payment retry attempt {} failed for invoice: {}", count, inv.getInvoiceNumber());
                }
            }
        }
    }

    /**
     * Daily cleanup logs job.
     * Deletes audit and usage logs older than 90 days to prevent index bloat.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2:00 AM
    @Transactional
    public void cleanupLogs() {
        log.info("Cleaning up historical logs older than 90 days...");
        OffsetDateTime threshold = OffsetDateTime.now().minusDays(90);

        // Standard custom delete methods using queries or direct deletion sweeps
        // To preserve database integrity, simple loops or bulk queries are used.
        try {
            List<AuditLog> oldAudits = auditLogRepository.findAll().stream()
                    .filter(a -> a.getCreatedAt().isBefore(threshold))
                    .collect(Collectors.toList());
            auditLogRepository.deleteAll(oldAudits);

            List<UsageLog> oldUsage = usageLogRepository.findAll().stream()
                    .filter(u -> u.getLoggedAt().isBefore(threshold))
                    .collect(Collectors.toList());
            usageLogRepository.deleteAll(oldUsage);

            log.info("Cleanup completed. Deleted {} audit rows and {} usage log rows.", oldAudits.size(), oldUsage.size());
        } catch (Exception e) {
            log.error("Failed to clean up logs: {}", e.getMessage());
        }
    }
}
