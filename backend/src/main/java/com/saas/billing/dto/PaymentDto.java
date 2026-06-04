package com.saas.billing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private UUID id;
    private UUID organizationId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String failureMessage;
    private OffsetDateTime createdAt;
}
