package com.saas.billing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String billingInterval;
    private Integer trialPeriodDays;
    private Boolean isActive;
    private List<PlanFeatureDto> features;
}
