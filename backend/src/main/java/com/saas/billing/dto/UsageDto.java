package com.saas.billing.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageDto {
    private String metricKey;
    private Integer quantity;
    private Integer maxLimit; // e.g. 5, 50, or -1 for unlimited
    private Double usagePercentage;
}
