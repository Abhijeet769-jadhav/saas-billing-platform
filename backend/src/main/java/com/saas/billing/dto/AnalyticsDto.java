package com.saas.billing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsDto {
    private BigDecimal mrr;
    private BigDecimal arr;
    private Double churnRate; // in percentage, e.g. 5.2
    private Double growthRate; // e.g. 12.5
    private Long activeSubscribers;
    private Long trialSubscribers;
    private Double paymentSuccessRate; // e.g. 98.4
    private Map<String, Long> planDistribution; // Plan Name -> Active Count
    private Map<String, BigDecimal> revenueByPlan; // Plan Name -> Total Revenue Amount
    private Map<String, BigDecimal> revenueByMonth; // YYYY-MM -> Monthly Revenue
}
