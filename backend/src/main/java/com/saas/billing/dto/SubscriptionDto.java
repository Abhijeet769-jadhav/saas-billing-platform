package com.saas.billing.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {
    private UUID id;
    private UUID organizationId;
    private UUID planId;
    private String planName;
    private String status;
    private String stripeSubscriptionId;
    private OffsetDateTime currentPeriodStart;
    private OffsetDateTime currentPeriodEnd;
    private OffsetDateTime trialStart;
    private OffsetDateTime trialEnd;
    private Boolean cancelAtPeriodEnd;
    private OffsetDateTime canceledAt;
    private OffsetDateTime endedAt;
}
