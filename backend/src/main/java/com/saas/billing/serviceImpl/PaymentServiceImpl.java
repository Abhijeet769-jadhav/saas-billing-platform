package com.saas.billing.serviceImpl;

import com.saas.billing.dto.PaymentDto;
import com.saas.billing.entity.*;
import com.saas.billing.exception.BadRequestException;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.mapper.DtoMapper;
import com.saas.billing.repository.*;
import com.saas.billing.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OrganizationRepository organizationRepository;
    private final PlanRepository planRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    @Value("${app.stripe.api-key}")
    private String stripeApiKey;

    @Value("${app.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    private boolean isStripeMocked() {
        return stripeApiKey == null || stripeApiKey.startsWith("sk_test_mock");
    }

    @Override
    @Transactional
    public String createCheckoutSession(UUID organizationId, UUID planId, String successUrl, String cancelUrl) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        if (isStripeMocked()) {
            log.info("Stripe is mocked. Simulating checkout session for plan: {}", plan.getName());
            return successUrl + "?session_id=mock_session_" + UUID.randomUUID().toString().replace("-", "");
        }

        try {
            Stripe.apiKey = stripeApiKey;

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(organization.getSlug() + "@customer.com")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(plan.getStripePriceId())
                            .setQuantity(1L)
                            .build())
                    .putMetadata("organizationId", organizationId.toString())
                    .putMetadata("planId", planId.toString())
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            log.error("Failed to create Stripe checkout session: {}", e.getMessage());
            throw new BadRequestException("Stripe checkout failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        Event event;
        if (isStripeMocked()) {
            log.info("Stripe mocked: bypassing webhook signature validation for raw payload");
            try {
                event = Event.GSON.fromJson(payload, Event.class);
            } catch (Exception e) {
                throw new BadRequestException("Invalid mock json payload: " + e.getMessage());
            }
        } else {
            try {
                event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            } catch (Exception e) {
                log.error("Stripe signature validation failed: {}", e.getMessage());
                throw new BadRequestException("Webhook signature verification failed");
            }
        }

        String eventId = event.getId();

        // 1. Double Webhook Processing and De-duplication check
        if (webhookEventRepository.existsByStripeEventId(eventId)) {
            log.info("Stripe Webhook event {} already processed. Skipping...", eventId);
            return;
        }

        // Store webhook log
        WebhookEvent dbEvent = WebhookEvent.builder()
                .stripeEventId(eventId)
                .eventType(event.getType())
                .payload(payload)
                .status("RECEIVED")
                .build();
        dbEvent = webhookEventRepository.save(dbEvent);

        try {
            processWebhookEvent(event);
            dbEvent.setStatus("PROCESSED");
            dbEvent.setProcessedAt(OffsetDateTime.now());
            webhookEventRepository.save(dbEvent);
        } catch (Exception e) {
            log.error("Failed to process Stripe Webhook Event {}: {}", eventId, e.getMessage(), e);
            dbEvent.setStatus("FAILED");
            dbEvent.setErrorMessage(e.getMessage());
            webhookEventRepository.save(dbEvent);
            throw e;
        }
    }

    private void processWebhookEvent(Event event) {
        log.info("Processing Stripe event type: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "invoice.paid":
                handleInvoicePaid(event);
                break;
            case "invoice.payment_failed":
                handleInvoicePaymentFailed(event);
                break;
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;
            default:
                log.info("Stripe Webhook event type ignored: {}", event.getType());
                break;
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        // Extract checkout metadata
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) return;

        String orgIdStr = session.getMetadata().get("organizationId");
        String planIdStr = session.getMetadata().get("planId");
        String stripeSubId = session.getSubscription();

        if (orgIdStr != null && planIdStr != null) {
            UUID organizationId = UUID.fromString(orgIdStr);
            UUID planId = UUID.fromString(planIdStr);

            Organization org = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
            Plan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

            Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                    .orElse(Subscription.builder()
                            .organization(org)
                            .build());

            sub.setPlan(plan);
            sub.setStatus("ACTIVE");
            sub.setStripeSubscriptionId(stripeSubId);
            sub.setCurrentPeriodStart(OffsetDateTime.now());
            sub.setCurrentPeriodEnd(OffsetDateTime.now().plusMonths(plan.getBillingInterval().equalsIgnoreCase("yearly") ? 12 : 1));

            subscriptionRepository.save(sub);
            log.info("Successfully provisioned subscription via Checkout session completed for org: {}", organizationId);
        }
    }

    private void handleInvoicePaid(Event event) {
        com.stripe.model.Invoice stripeInvoice = (com.stripe.model.Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeInvoice == null) return;

        String stripeSubId = stripeInvoice.getSubscription();
        if (stripeSubId == null) return;

        Subscription sub = subscriptionRepository.findByStripeSubscriptionId(stripeSubId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found for Stripe Subscription ID: " + stripeSubId));

        // Create or update invoice in db
        Invoice invoice = invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId())
                .orElse(Invoice.builder()
                        .organization(sub.getOrganization())
                        .subscription(sub)
                        .stripeInvoiceId(stripeInvoice.getId())
                        .invoiceNumber("INV-SRV-" + System.currentTimeMillis())
                        .build());

        invoice.setAmountDue(BigDecimal.valueOf(stripeInvoice.getAmountDue()).divide(BigDecimal.valueOf(100)));
        invoice.setAmountPaid(BigDecimal.valueOf(stripeInvoice.getAmountPaid()).divide(BigDecimal.valueOf(100)));
        invoice.setSubtotal(BigDecimal.valueOf(stripeInvoice.getSubtotal()).divide(BigDecimal.valueOf(100)));
        invoice.setTotal(BigDecimal.valueOf(stripeInvoice.getTotal()).divide(BigDecimal.valueOf(100)));
        invoice.setStatus("PAID");
        invoice.setPaidAt(OffsetDateTime.now());
        invoice.setDueDate(OffsetDateTime.now().plusDays(7));
        invoice.setCurrency(stripeInvoice.getCurrency().toUpperCase());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Add line items
        InvoiceItem item = InvoiceItem.builder()
                .invoice(savedInvoice)
                .description("Service plan renewal: " + sub.getPlan().getName())
                .quantity(1)
                .unitAmount(savedInvoice.getTotal())
                .amount(savedInvoice.getTotal())
                .build();
        invoiceItemRepository.save(item);

        // Register payment
        Payment payment = Payment.builder()
                .organization(sub.getOrganization())
                .invoice(savedInvoice)
                .stripePaymentIntentId(stripeInvoice.getPaymentIntent())
                .amount(savedInvoice.getAmountPaid())
                .status("SUCCEEDED")
                .paymentMethod("card")
                .currency(savedInvoice.getCurrency())
                .build();
        paymentRepository.save(payment);

        // Renew period dates in local subscription
        sub.setStatus("ACTIVE");
        sub.setCurrentPeriodStart(OffsetDateTime.ofInstant(Instant.ofEpochSecond(stripeInvoice.getPeriodStart()), ZoneOffset.UTC));
        sub.setCurrentPeriodEnd(OffsetDateTime.ofInstant(Instant.ofEpochSecond(stripeInvoice.getPeriodEnd()), ZoneOffset.UTC));
        subscriptionRepository.save(sub);

        log.info("Invoice {} marked as PAID. Sub renewed to {}", stripeInvoice.getId(), sub.getCurrentPeriodEnd());
    }

    private void handleInvoicePaymentFailed(Event event) {
        com.stripe.model.Invoice stripeInvoice = (com.stripe.model.Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeInvoice == null) return;

        String stripeSubId = stripeInvoice.getSubscription();
        if (stripeSubId == null) return;

        Subscription sub = subscriptionRepository.findByStripeSubscriptionId(stripeSubId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        Invoice invoice = invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId())
                .orElse(Invoice.builder()
                        .organization(sub.getOrganization())
                        .subscription(sub)
                        .stripeInvoiceId(stripeInvoice.getId())
                        .invoiceNumber("INV-SRV-" + System.currentTimeMillis())
                        .build());

        invoice.setAmountDue(BigDecimal.valueOf(stripeInvoice.getAmountDue()).divide(BigDecimal.valueOf(100)));
        invoice.setSubtotal(BigDecimal.valueOf(stripeInvoice.getSubtotal()).divide(BigDecimal.valueOf(100)));
        invoice.setTotal(BigDecimal.valueOf(stripeInvoice.getTotal()).divide(BigDecimal.valueOf(100)));
        invoice.setStatus("OPEN");
        invoice.setDueDate(OffsetDateTime.now().plusDays(7));
        invoice.setCurrency(stripeInvoice.getCurrency().toUpperCase());

        invoiceRepository.save(invoice);

        // Register failed payment
        Payment payment = Payment.builder()
                .organization(sub.getOrganization())
                .invoice(invoice)
                .stripePaymentIntentId(stripeInvoice.getPaymentIntent())
                .amount(invoice.getTotal())
                .status("FAILED")
                .currency(invoice.getCurrency())
                .failureMessage("Card declined / funds unavailable")
                .build();
        paymentRepository.save(payment);

        // Update local subscription status to PAST_DUE
        sub.setStatus("PAST_DUE");
        subscriptionRepository.save(sub);

        log.warn("Invoice payment failed for subscription {}. Status updated to PAST_DUE", stripeSubId);
    }

    private void handleSubscriptionDeleted(Event event) {
        com.stripe.model.Subscription stripeSub = (com.stripe.model.Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeSub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId()).ifPresent(sub -> {
            sub.setStatus("EXPIRED");
            sub.setEndedAt(OffsetDateTime.now());
            subscriptionRepository.save(sub);
            log.info("Stripe subscription {} deleted. Deactivated local access status to EXPIRED", stripeSub.getId());
        });
    }

    @Override
    @Transactional
    public void refundPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getStatus().equals("SUCCEEDED")) {
            throw new BadRequestException("Only succeeded payments can be refunded");
        }

        // Simulating refund
        payment.setStatus("REFUNDED");
        payment.setRefundedAmount(payment.getAmount());
        paymentRepository.save(payment);

        if (payment.getInvoice() != null) {
            Invoice inv = payment.getInvoice();
            inv.setStatus("VOID");
            invoiceRepository.save(inv);
        }
        log.info("Refund processed successfully for payment: {}", paymentId);
    }

    @Override
    @Transactional
    public void retryPayment(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getStatus().equals("PAID")) {
            throw new BadRequestException("Invoice is already paid");
        }

        // Simulating immediate retry success
        invoice.setStatus("PAID");
        invoice.setAmountPaid(invoice.getAmountDue());
        invoice.setPaidAt(OffsetDateTime.now());
        invoiceRepository.save(invoice);

        if (invoice.getSubscription() != null) {
            Subscription sub = invoice.getSubscription();
            sub.setStatus("ACTIVE");
            subscriptionRepository.save(sub);
        }

        log.info("Retry payment succeeded for invoice: {}", invoiceId);
    }

    @Override
    public List<PaymentDto> getPaymentHistory(UUID organizationId) {
        return paymentRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(DtoMapper::toPaymentDto)
                .collect(Collectors.toList());
    }
}
