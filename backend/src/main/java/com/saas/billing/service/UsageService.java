package com.saas.billing.service;

import com.saas.billing.dto.UsageDto;

import java.util.List;
import java.util.UUID;

public interface UsageService {
    void logUsage(UUID organizationId, String metricKey, int quantity);
    List<UsageDto> getCurrentUsage(UUID organizationId);
    boolean checkLimit(UUID organizationId, String metricKey, int requestedIncrement);
}
