package com.saas.billing.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeatureDto {
    private String featureKey;
    private String featureValue;
}
