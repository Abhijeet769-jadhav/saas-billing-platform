package com.saas.billing.service;

import com.saas.billing.dto.SubscriptionChangeRequest;
import com.saas.billing.dto.SubscriptionDto;
import com.saas.billing.entity.*;
import com.saas.billing.exception.ResourceNotFoundException;
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
class SubscriptionServiceTest {

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
                .amount(BigDecimal.valueOf(19))
                .billingInterval("monthly")
                .trialPeriodDays(14)
                .features(new ArrayList<>())
                .build();

        proPlan = Plan.builder()
                .id(UUID.randomUUID())
                .name("Pro Plan")
                .amount(BigDecimal.valueOf(49))
                .billingInterval("monthly")
                .features(new ArrayList<>())
                .build();

        basicPlan.getFeatures().add(
                PlanFeature.builder()
                        .plan(basicPlan)
                        .featureKey("max_users")
                        .featureValue("5")
                        .build()
        );

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
    void shouldCreateNewSubscriptionSuccessfully() {

        when(organizationRepository.findById(organization.getId()))
                .thenReturn(Optional.of(organization));

        when(planRepository.findById(basicPlan.getId()))
                .thenReturn(Optional.of(basicPlan));

        when(subscriptionRepository.findByOrganizationId(organization.getId()))
                .thenReturn(Optional.empty());

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(subscription);

        SubscriptionDto result = subscriptionService.subscribe(
                organization.getId(),
                new SubscriptionChangeRequest(basicPlan.getId(), null)
        );

        assertNotNull(result);
        assertEquals("TRIAL", result.getStatus());
        assertEquals("Basic Plan", result.getPlanName());

        verify(subscriptionRepository).save(any(Subscription.class));
        verify(subscriptionHistoryRepository).save(any(SubscriptionHistory.class));
    }

    @Test
    void shouldThrowExceptionWhenOrganizationDoesNotExist() {

        when(organizationRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                subscriptionService.subscribe(
                        UUID.randomUUID(),
                        new SubscriptionChangeRequest(basicPlan.getId(), null)
                ));

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
void shouldUpgradeExistingSubscriptionWhenAlreadySubscribed() {

    when(organizationRepository.findById(organization.getId()))
            .thenReturn(Optional.of(organization));

    when(planRepository.findById(proPlan.getId()))
            .thenReturn(Optional.of(proPlan));

    when(subscriptionRepository.findByOrganizationId(organization.getId()))
            .thenReturn(Optional.of(subscription));

    when(subscriptionRepository.save(any(Subscription.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    SubscriptionDto result = subscriptionService.subscribe(
            organization.getId(),
            new SubscriptionChangeRequest(proPlan.getId(), null)
    );

    assertNotNull(result);
    assertEquals("ACTIVE", result.getStatus());
    assertEquals("Pro Plan", result.getPlanName());

    verify(subscriptionRepository).save(any(Subscription.class));
    verify(subscriptionHistoryRepository).save(any(SubscriptionHistory.class));
}

  
    @Test
    void shouldUpgradeSubscriptionSuccessfully() {

        when(subscriptionRepository.findByOrganizationId(organization.getId()))
                .thenReturn(Optional.of(subscription));

        when(planRepository.findById(proPlan.getId()))
                .thenReturn(Optional.of(proPlan));

        when(subscriptionRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionDto result = subscriptionService.upgrade(
                organization.getId(),
                new SubscriptionChangeRequest(proPlan.getId(), null)
        );

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        assertEquals("Pro Plan", result.getPlanName());

        verify(subscriptionHistoryRepository).save(any(SubscriptionHistory.class));
    }

    @Test
    void shouldReturnTrueWhenUsageIsWithinPlanLimit() {

        when(subscriptionRepository.findByOrganizationId(organization.getId()))
                .thenReturn(Optional.of(subscription));

        boolean access = subscriptionService.verifyAccess(
                organization.getId(),
                "max_users",
                3
        );

        assertTrue(access);
    }

    @Test
    void shouldReturnFalseWhenUsageExceedsPlanLimit() {

        when(subscriptionRepository.findByOrganizationId(organization.getId()))
                .thenReturn(Optional.of(subscription));

        boolean access = subscriptionService.verifyAccess(
                organization.getId(),
                "max_users",
                6
        );

        assertFalse(access);
    }

   @Test
void shouldReturnFalseWhenFeatureDoesNotExist() {

    when(subscriptionRepository.findByOrganizationId(organization.getId()))
            .thenReturn(Optional.of(subscription));

    boolean access = subscriptionService.verifyAccess(
            organization.getId(),
            "unknown_feature",
            999
    );

    assertFalse(access);
}
}