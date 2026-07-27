package com.saas.billing.service;

import com.saas.billing.dto.UsageDto;
import com.saas.billing.entity.Organization;
import com.saas.billing.entity.Plan;
import com.saas.billing.entity.PlanFeature;
import com.saas.billing.entity.Subscription;
import com.saas.billing.entity.UsageLog;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.SubscriptionRepository;
import com.saas.billing.repository.UsageLogRepository;
import com.saas.billing.serviceImpl.UsageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @Mock
    private UsageLogRepository usageLogRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private UsageServiceImpl usageService;

    private UUID organizationId;
    private Organization organization;
    private Subscription subscription;
    private Plan plan;
    private PlanFeature apiFeature;
    private UsageLog usageLog;

    @BeforeEach
    void setUp() {

        organizationId = UUID.randomUUID();

        organization = Organization.builder()
                .id(organizationId)
                .name("Test Organization")
                .build();

        apiFeature = PlanFeature.builder()
                .featureKey("API_CALLS")
                .featureValue("1000")
                .build();

        plan = Plan.builder()
                .features(List.of(apiFeature))
                .build();

        subscription = Subscription.builder()
                .organization(organization)
                .plan(plan)
                .currentPeriodStart(OffsetDateTime.now().minusDays(5))
                .build();

        usageLog = UsageLog.builder()
                .organization(organization)
                .metricKey("API_CALLS")
                .quantity(50)
                .build();
    }

    @Test
    void shouldLogUsageSuccessfully() {

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(subscription));

        when(usageLogRepository.save(any(UsageLog.class)))
                .thenReturn(usageLog);

        assertDoesNotThrow(() ->
                usageService.logUsage(
                        organizationId,
                        "API_CALLS",
                        50));

        verify(subscriptionRepository)
                .findByOrganizationId(organizationId);

        verify(usageLogRepository)
                .save(any(UsageLog.class));
    }

    @Test
    void shouldThrowWhenLoggingUsageWithoutSubscription() {

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> usageService.logUsage(
                        organizationId,
                        "API_CALLS",
                        10)
        );

        verify(usageLogRepository, never()).save(any());
    }

    @Test
    void shouldReturnCurrentUsageSuccessfully() {

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(subscription));

        when(usageLogRepository.sumQuantityByOrganizationAndMetricSince(
                eq(organizationId),
                eq("API_CALLS"),
                any(OffsetDateTime.class)))
                .thenReturn(50);

        List<UsageDto> result =
                usageService.getCurrentUsage(organizationId);

        assertNotNull(result);
        assertEquals(1, result.size());

        UsageDto dto = result.get(0);

        assertEquals("API_CALLS", dto.getMetricKey());
        assertEquals(50, dto.getQuantity());
        assertEquals(1000, dto.getMaxLimit());
        assertEquals(5.0, dto.getUsagePercentage());

        verify(subscriptionRepository)
                .findByOrganizationId(organizationId);
    }    @Test
    void shouldReturnCurrentUsageForBooleanFeature() {

        PlanFeature booleanFeature = PlanFeature.builder()
                .featureKey("CUSTOM_BRANDING")
                .featureValue("true")
                .build();

        plan.setFeatures(List.of(booleanFeature));

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(subscription));

        when(usageLogRepository.sumQuantityByOrganizationAndMetricSince(
                eq(organizationId),
                eq("CUSTOM_BRANDING"),
                any(OffsetDateTime.class)))
                .thenReturn(0);

        List<UsageDto> result =
                usageService.getCurrentUsage(organizationId);

        assertNotNull(result);
        assertEquals(1, result.size());

        UsageDto dto = result.get(0);

        assertEquals("CUSTOM_BRANDING", dto.getMetricKey());
        assertEquals(1, dto.getMaxLimit());

        verify(subscriptionRepository)
                .findByOrganizationId(organizationId);
    }

    @Test
    void shouldAllowUsageWhenWithinLimit() {

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(subscription));

        when(usageLogRepository.sumQuantityByOrganizationAndMetricSince(
                eq(organizationId),
                eq("API_CALLS"),
                any(OffsetDateTime.class)))
                .thenReturn(500);

        assertTrue(
                usageService.checkLimit(
                        organizationId,
                        "API_CALLS",
                        100));
    }

    @Test
    void shouldRejectUsageWhenLimitExceeded() {

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(subscription));

        when(usageLogRepository.sumQuantityByOrganizationAndMetricSince(
                eq(organizationId),
                eq("API_CALLS"),
                any(OffsetDateTime.class)))
                .thenReturn(950);

        assertFalse(
                usageService.checkLimit(
                        organizationId,
                        "API_CALLS",
                        100));
    }

    @Test
    void shouldAllowUnlimitedUsage() {

        apiFeature.setFeatureValue("999999");

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(subscription));

        when(usageLogRepository.sumQuantityByOrganizationAndMetricSince(
                any(),
                any(),
                any()))
                .thenReturn(500000);

        assertTrue(
                usageService.checkLimit(
                        organizationId,
                        "API_CALLS",
                        500000));
    }

    @Test
    void shouldAllowBooleanFeature() {

        PlanFeature feature = PlanFeature.builder()
                .featureKey("CUSTOM_BRANDING")
                .featureValue("true")
                .build();

        plan.setFeatures(List.of(feature));

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(subscription));

        assertTrue(
                usageService.checkLimit(
                        organizationId,
                        "CUSTOM_BRANDING",
                        1));
    }

    @Test
    void shouldThrowWhenSubscriptionMissingForCurrentUsage() {

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> usageService.getCurrentUsage(organizationId));
    }

    @Test
    void shouldThrowWhenSubscriptionMissingForCheckLimit() {

        when(subscriptionRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> usageService.checkLimit(
                        organizationId,
                        "API_CALLS",
                        1));
    }
}