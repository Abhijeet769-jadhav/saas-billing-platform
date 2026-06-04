package com.saas.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false, length = 50)
    private String status; // TRIAL, ACTIVE, PAST_DUE, GRACE_PERIOD, PAUSED, CANCELLED, EXPIRED

    @Column(name = "stripe_subscription_id", unique = true)
    private String stripeSubscriptionId;

    @Column(name = "current_period_start", nullable = false)
    private OffsetDateTime currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private OffsetDateTime currentPeriodEnd;

    @Column(name = "trial_start")
    private OffsetDateTime trialStart;

    @Column(name = "trial_end")
    private OffsetDateTime trialEnd;

    @Column(name = "cancel_at_period_end")
    private Boolean cancelAtPeriodEnd = false;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
