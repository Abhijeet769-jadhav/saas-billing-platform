package com.saas.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "plan_features",
    uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "feature_key"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "feature_key", nullable = false, length = 100)
    private String featureKey;

    @Column(name = "feature_value", nullable = false)
    private String featureValue;
}
