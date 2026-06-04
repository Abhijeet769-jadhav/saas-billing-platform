package com.saas.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupon_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType; // PERCENTAGE, FIXED_AMOUNT

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(length = 3)
    private String currency = "USD";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
