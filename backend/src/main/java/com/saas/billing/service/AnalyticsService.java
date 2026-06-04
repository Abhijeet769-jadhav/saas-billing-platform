package com.saas.billing.service;

import com.saas.billing.dto.AnalyticsDto;

import java.io.ByteArrayInputStream;
import java.util.UUID;

public interface AnalyticsService {
    AnalyticsDto getPlatformAnalytics();
    AnalyticsDto getOrganizationAnalytics(UUID organizationId);
    ByteArrayInputStream exportRevenueReportCsv();
}
