package com.saas.billing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionChangeRequest {

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    private String couponCode;
}
