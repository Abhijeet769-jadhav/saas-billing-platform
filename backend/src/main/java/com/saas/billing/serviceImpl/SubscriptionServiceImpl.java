package com.saas.billing.serviceImpl;

import com.saas.billing.dto.SubscriptionChangeRequest;
import com.saas.billing.dto.SubscriptionDto;
import com.saas.billing.entity.*;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.mapper.DtoMapper;
import com.saas.billing.repository.*;
import com.saas.billing.service.SubscriptionService;
import com.stripe.Stripe;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final OrganizationRepository organizationRepository;
    private final PlanRepository planRepository;
    private final UsageLogRepository usageLogRepository;

    @Value("${app.stripe.api-key}")
    private String stripeApiKey;

    private boolean isStripeMocked() {
        return stripeApiKey == null || stripeApiKey.startsWith("sk_test_mock");
    }

    @Override
    public SubscriptionDto getSubscription(UUID organizationId) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found for organization: " + organizationId));
        return DtoMapper.toSubscriptionDto(sub);
    }

    @Override
    @Transactional
    public SubscriptionDto subscribe(UUID organizationId, SubscriptionChangeRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        // If there's an existing subscription, upgrade/downgrade instead
        if (subscriptionRepository.findByOrganizationId(organizationId).isPresent()) {
            return upgrade(organizationId, request);
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime periodEnd = now.plusMonths(plan.getBillingInterval().equalsIgnoreCase("yearly") ? 12 : 1);

        Subscription sub = Subscription.builder()
                .organization(organization)
                .plan(plan)
                .status(plan.getTrialPeriodDays() > 0 ? "TRIAL" : "ACTIVE")
                .currentPeriodStart(now)
                .currentPeriodEnd(periodEnd)
                .trialStart(plan.getTrialPeriodDays() > 0 ? now : null)
                .trialEnd(plan.getTrialPeriodDays() > 0 ? now.plusDays(plan.getTrialPeriodDays()) : null)
                .cancelAtPeriodEnd(false)
                .stripeSubscriptionId(isStripeMocked() ? "sub_mock_" + UUID.randomUUID().toString().replace("-", "") : null)
                .build();

        Subscription savedSub = subscriptionRepository.save(sub);

        // Record history
        subscriptionHistoryRepository.save(SubscriptionHistory.builder()
                .subscription(savedSub)
                .organization(organization)
                .plan(plan)
                .action("START")
                .statusBefore(null)
                .statusAfter(savedSub.getStatus())
                .notes("Subscribed to " + plan.getName())
                .build());

        return DtoMapper.toSubscriptionDto(savedSub);
    }

    @Override
    @Transactional
    public SubscriptionDto upgrade(UUID organizationId, SubscriptionChangeRequest request) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription to upgrade"));

        Plan newPlan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Target plan not found"));

        String oldStatus = sub.getStatus();
        Plan oldPlan = sub.getPlan();

        // Perform upgrade changes
        sub.setPlan(newPlan);
        sub.setStatus("ACTIVE");
        sub.setCurrentPeriodStart(OffsetDateTime.now());
        sub.setCurrentPeriodEnd(OffsetDateTime.now().plusMonths(newPlan.getBillingInterval().equalsIgnoreCase("yearly") ? 12 : 1));

        // If Stripe keys are present and it's not a mock setup, update Stripe
        if (!isStripeMocked() && sub.getStripeSubscriptionId() != null) {
            try {
                Stripe.apiKey = stripeApiKey;
                com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(sub.getStripeSubscriptionId());
                SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                        .addItem(SubscriptionUpdateParams.Item.builder()
                                .setId(stripeSub.getItems().getData().get(0).getId())
                                .setPrice(newPlan.getStripePriceId())
                                .build())
                        .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.CREATE_PRORATIONS)
                        .build();
                stripeSub.update(params);
            } catch (Exception e) {
                log.error("Failed to update Stripe subscription: {}", e.getMessage());
            }
        }

        Subscription updated = subscriptionRepository.save(sub);

        // Log history
        subscriptionHistoryRepository.save(SubscriptionHistory.builder()
                .subscription(updated)
                .organization(updated.getOrganization())
                .plan(newPlan)
                .action("UPGRADE")
                .statusBefore(oldStatus)
                .statusAfter("ACTIVE")
                .notes("Upgraded from " + oldPlan.getName() + " to " + newPlan.getName())
                .build());

        return DtoMapper.toSubscriptionDto(updated);
    }

    @Override
    @Transactional
    public SubscriptionDto downgrade(UUID organizationId, SubscriptionChangeRequest request) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription to downgrade"));

        Plan newPlan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Target plan not found"));

        String oldStatus = sub.getStatus();
        Plan oldPlan = sub.getPlan();

        // Set state values
        sub.setPlan(newPlan);
        sub.setStatus("ACTIVE");

        if (!isStripeMocked() && sub.getStripeSubscriptionId() != null) {
            try {
                Stripe.apiKey = stripeApiKey;
                com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(sub.getStripeSubscriptionId());
                SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                        .addItem(SubscriptionUpdateParams.Item.builder()
                                .setId(stripeSub.getItems().getData().get(0).getId())
                                .setPrice(newPlan.getStripePriceId())
                                .build())
                        .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.NONE)
                        .build();
                stripeSub.update(params);
            } catch (Exception e) {
                log.error("Failed to downgrade Stripe subscription: {}", e.getMessage());
            }
        }

        Subscription updated = subscriptionRepository.save(sub);

        // Log history
        subscriptionHistoryRepository.save(SubscriptionHistory.builder()
                .subscription(updated)
                .organization(updated.getOrganization())
                .plan(newPlan)
                .action("DOWNGRADE")
                .statusBefore(oldStatus)
                .statusAfter("ACTIVE")
                .notes("Downgraded from " + oldPlan.getName() + " to " + newPlan.getName())
                .build());

        return DtoMapper.toSubscriptionDto(updated);
    }

    @Override
    @Transactional
    public void cancel(UUID organizationId) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        String oldStatus = sub.getStatus();
        sub.setStatus("CANCELLED");
        sub.setCancelAtPeriodEnd(true);
        sub.setCanceledAt(OffsetDateTime.now());

        if (!isStripeMocked() && sub.getStripeSubscriptionId() != null) {
            try {
                Stripe.apiKey = stripeApiKey;
                com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(sub.getStripeSubscriptionId());
                stripeSub.cancel();
            } catch (Exception e) {
                log.error("Failed to cancel Stripe subscription: {}", e.getMessage());
            }
        }

        subscriptionRepository.save(sub);

        subscriptionHistoryRepository.save(SubscriptionHistory.builder()
                .subscription(sub)
                .organization(sub.getOrganization())
                .plan(sub.getPlan())
                .action("CANCEL")
                .statusBefore(oldStatus)
                .statusAfter("CANCELLED")
                .notes("Subscription cancelled by user")
                .build());
    }

    @Override
    @Transactional
    public void pause(UUID organizationId) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        String oldStatus = sub.getStatus();
        sub.setStatus("PAUSED");
        subscriptionRepository.save(sub);

        subscriptionHistoryRepository.save(SubscriptionHistory.builder()
                .subscription(sub)
                .organization(sub.getOrganization())
                .plan(sub.getPlan())
                .action("PAUSE")
                .statusBefore(oldStatus)
                .statusAfter("PAUSED")
                .notes("Subscription paused")
                .build());
    }

    @Override
    @Transactional
    public void resume(UUID organizationId) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        String oldStatus = sub.getStatus();
        sub.setStatus("ACTIVE");
        subscriptionRepository.save(sub);

        subscriptionHistoryRepository.save(SubscriptionHistory.builder()
                .subscription(sub)
                .organization(sub.getOrganization())
                .plan(sub.getPlan())
                .action("RESUME")
                .statusBefore(oldStatus)
                .statusAfter("ACTIVE")
                .notes("Subscription resumed")
                .build());
    }

    @Override
    @Transactional
    public void renew(UUID organizationId) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        String oldStatus = sub.getStatus();
        OffsetDateTime oldPeriodEnd = sub.getCurrentPeriodEnd();
        sub.setStatus("ACTIVE");
        sub.setCurrentPeriodStart(oldPeriodEnd);
        sub.setCurrentPeriodEnd(oldPeriodEnd.plusMonths(sub.getPlan().getBillingInterval().equalsIgnoreCase("yearly") ? 12 : 1));
        subscriptionRepository.save(sub);

        subscriptionHistoryRepository.save(SubscriptionHistory.builder()
                .subscription(sub)
                .organization(sub.getOrganization())
                .plan(sub.getPlan())
                .action("RENEW")
                .statusBefore(oldStatus)
                .statusAfter("ACTIVE")
                .notes("Subscription renewed for next cycle")
                .build());
    }

    @Override
    public boolean verifyAccess(UUID organizationId, String featureKey, int currentUsage) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        // Free trials or cancelled/expired accounts checking
        if (sub.getStatus().equals("EXPIRED") || sub.getStatus().equals("FAILED_PAYMENT")) {
            return false;
        }

        // Get limits from features
        Plan plan = sub.getPlan();
        for (PlanFeature feature : plan.getFeatures()) {
            if (feature.getFeatureKey().equalsIgnoreCase(featureKey)) {
                try {
                    int limit = Integer.parseInt(feature.getFeatureValue());
                    return currentUsage < limit;
                } catch (NumberFormatException e) {
                    // It's a boolean check, e.g. custom_domain = true
                    return Boolean.parseBoolean(feature.getFeatureValue());
                }
            }
        }
        return false;
    }
}
