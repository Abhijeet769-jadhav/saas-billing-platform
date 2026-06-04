package com.saas.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "stripe_payment_intent_id", unique = true)
    private String stripePaymentIntentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "USD";

    @Column(nullable = false, length = 50)
    private String status; // PENDING, SUCCEEDED, FAILED, REFUNDED

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "failure_message", columnDefinition = "TEXT")
    private String failureMessage;

    @Column(name = "refunded_amount", precision = 12, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
