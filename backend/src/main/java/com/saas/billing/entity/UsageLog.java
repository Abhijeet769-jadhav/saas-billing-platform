package com.saas.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "usage_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "metric_key", nullable = false, length = 100)
    private String metricKey; // e.g. api_calls, storage_bytes

    @Column(nullable = false)
    private Integer quantity;

    @CreationTimestamp
    @Column(name = "logged_at", updatable = false)
    private OffsetDateTime loggedAt;
}
