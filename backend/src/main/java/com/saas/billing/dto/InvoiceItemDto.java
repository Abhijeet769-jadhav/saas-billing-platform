package com.saas.billing.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemDto {
    private String description;
    private Integer quantity;
    private BigDecimal unitAmount;
    private BigDecimal amount;
}
