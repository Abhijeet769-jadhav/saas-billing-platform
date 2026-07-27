package com.saas.billing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    @JsonIgnore
    private Organization organization;

    @Column(name = "tax_registration_number")
    private String taxRegistrationNumber;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "country", length = 100)
    private String country = "US";

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}