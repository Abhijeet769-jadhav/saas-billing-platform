package com.saas.billing.service;

import com.saas.billing.dto.SubscriptionChangeRequest;
import com.saas.billing.dto.SubscriptionDto;
import com.saas.billing.entity.*;
import com.saas.billing.repository.*;
import com.saas.billing.serviceImpl.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private UsageLogRepository usageLogRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Organization organization;
    private Plan basicPlan;
    private Plan proPlan;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(UUID.randomUUID())
                .name("Acme Corp")
                .slug("acme-corp")
                .build();

        basicPlan = Plan.builder()
                .id(UUID.randomUUID())
                .name("Basic Plan")
                .amount(BigDecimal.valueOf(19.00))
                .billingInterval("monthly")
                .trialPeriodDays(14)
                .features(new ArrayList<>())
                .build();

        proPlan = Plan.builder()
                .id(UUID.randomUUID())
                .name("Pro Plan")
                .amount(BigDecimal.valueOf(49.00))
                .billingInterval("monthly")
                .features(new ArrayList<>())
                .build();

        // Add features to basic plan
        basicPlan.getFeatures().add(PlanFeature.builder()
                .plan(basicPlan)
                .featureKey("max_users")
                .featureValue("5")
                .build());

        subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .plan(basicPlan)
                .status("TRIAL")
                .currentPeriodStart(OffsetDateTime.now())
                .currentPeriodEnd(OffsetDateTime.now().plusDays(14))
                .build();
    }

    @Test
    void testSubscribe_Success_NewSubscription() {
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(planRepository.findById(basicPlan.getId())).thenReturn(Optional.of(basicPlan));
        when(subscriptionRepository.findByOrganizationId(organization.getId())).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        SubscriptionChangeRequest request = new SubscriptionChangeRequest(basicPlan.getId(), null);
        SubscriptionDto result = subscriptionService.subscribe(organization.getId(), request);

        assertNotNull(result);
        assertEquals("TRIAL", result.getStatus());
        assertEquals("Basic Plan", result.getPlanName());
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(subscriptionHistoryRepository, times(1)).save(any(SubscriptionHistory.class));
    }

    @Test
    void testUpgrade_Success() {
        when(subscriptionRepository.findByOrganizationId(organization.getId())).thenReturn(Optional.of(subscription));
        when(planRepository.findById(proPlan.getId())).thenReturn(Optional.of(proPlan));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionChangeRequest request = new SubscriptionChangeRequest(proPlan.getId(), null);
        SubscriptionDto result = subscriptionService.upgrade(organization.getId(), request);

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        assertEquals("Pro Plan", result.getPlanName());
        verify(subscriptionHistoryRepository, times(1)).save(any(SubscriptionHistory.class));
    }

    @Test
    void testVerifyAccess_UnderLimit_ReturnsTrue() {
        when(subscriptionRepository.findByOrganizationId(organization.getId())).thenReturn(Optional.of(subscription));

        // Under user limit of 5 (e.g. current usage is 3)
        boolean hasAccess = subscriptionService.verifyAccess(organization.getId(), "max_users", 3);
        assertTrue(hasAccess);
    }

    @Test
    void testVerifyAccess_OverLimit_ReturnsFalse() {
        when(subscriptionRepository.findByOrganizationId(organization.getId())).thenReturn(Optional.of(subscription));

        // Over limit of 5 (e.g. current usage is 6)
        boolean hasAccess = subscriptionService.verifyAccess(organization.getId(), "max_users", 6);
        assertFalse(hasAccess);
    }
}
