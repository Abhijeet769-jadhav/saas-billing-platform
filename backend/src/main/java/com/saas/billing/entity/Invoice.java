package com.saas.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 100)
    private String invoiceNumber; // INV-YYYYMMDD-XXXX

    @Column(name = "stripe_invoice_id", unique = true)
    private String stripeInvoiceId;

    @Column(name = "amount_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(length = 3)
    private String currency = "USD";

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal cgst = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal sgst = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal igst = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 50)
    private String status; // DRAFT, OPEN, PAID, UNCOLLECTIBLE, VOID

    @Column(name = "billing_reason")
    private String billingReason;

    @Column(name = "pdf_url", length = 512)
    private String pdfUrl;

    @Column(name = "due_date", nullable = false)
    private OffsetDateTime dueDate;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
