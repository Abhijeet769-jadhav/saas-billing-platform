package com.saas.billing.serviceImpl;

import com.saas.billing.dto.UsageDto;
import com.saas.billing.entity.*;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.SubscriptionRepository;
import com.saas.billing.repository.UsageLogRepository;
import com.saas.billing.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageServiceImpl implements UsageService {

    private final UsageLogRepository usageLogRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public void logUsage(UUID organizationId, String metricKey, int quantity) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription for organization: " + organizationId));

        UsageLog logEntry = UsageLog.builder()
                .organization(sub.getOrganization())
                .metricKey(metricKey)
                .quantity(quantity)
                .build();

        usageLogRepository.save(logEntry);
        log.debug("Logged {} usage of {} for org {}", quantity, metricKey, organizationId);
    }

    @Override
    public List<UsageDto> getCurrentUsage(UUID organizationId) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("No subscription found"));

        Plan plan = sub.getPlan();
        OffsetDateTime since = sub.getCurrentPeriodStart();

        List<UsageDto> usageList = new ArrayList<>();

        for (PlanFeature feature : plan.getFeatures()) {
            String key = feature.getFeatureKey();
            int currentSum = usageLogRepository.sumQuantityByOrganizationAndMetricSince(organizationId, key, since);

            int limit = -1;
            try {
                limit = Integer.parseInt(feature.getFeatureValue());
            } catch (NumberFormatException e) {
                // Feature is a boolean flag (e.g. custom_domain = true -> limit is 1 or -1)
                limit = Boolean.parseBoolean(feature.getFeatureValue()) ? 1 : 0;
            }

            double percentage = 0.0;
            if (limit > 0) {
                percentage = ((double) currentSum / limit) * 100.0;
            } else if (limit == 0) {
                percentage = 100.0;
            }

            usageList.add(UsageDto.builder()
                    .metricKey(key)
                    .quantity(currentSum)
                    .maxLimit(limit)
                    .usagePercentage(percentage)
                    .build());
        }

        return usageList;
    }

    @Override
    public boolean checkLimit(UUID organizationId, String metricKey, int requestedIncrement) {
        Subscription sub = subscriptionRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription"));

        Plan plan = sub.getPlan();
        OffsetDateTime since = sub.getCurrentPeriodStart();

        int currentSum = usageLogRepository.sumQuantityByOrganizationAndMetricSince(organizationId, metricKey, since);

        for (PlanFeature feature : plan.getFeatures()) {
            if (feature.getFeatureKey().equalsIgnoreCase(metricKey)) {
                try {
                    int limit = Integer.parseInt(feature.getFeatureValue());
                    // 999999 indicates unlimited in database seed values
                    if (limit >= 999999) return true;
                    return (currentSum + requestedIncrement) <= limit;
                } catch (NumberFormatException e) {
                    return Boolean.parseBoolean(feature.getFeatureValue());
                }
            }
        }
        return false;
    }
}
