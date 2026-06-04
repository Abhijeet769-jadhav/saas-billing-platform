package com.saas.billing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDto {
    private UUID id;
    private UUID organizationId;
    private String invoiceNumber;
    private String stripeInvoiceId;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private String currency;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String status;
    private String billingReason;
    private String pdfUrl;
    private OffsetDateTime dueDate;
    private OffsetDateTime paidAt;
    private List<InvoiceItemDto> items;
    private OffsetDateTime createdAt;
}
