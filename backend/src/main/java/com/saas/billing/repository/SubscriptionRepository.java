package com.saas.billing.repository;

import com.saas.billing.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    @Query("""
    SELECT s
    FROM Subscription s
    JOIN FETCH s.plan
    JOIN FETCH s.organization
    WHERE s.organization.id = :organizationId
    """)
Optional<Subscription> findByOrganizationId(
        @Param("organizationId") UUID organizationId);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    List<Subscription> findByStatus(String status);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'TRIAL' AND s.trialEnd < :now")
    List<Subscription> findExpiredTrials(@Param("now") OffsetDateTime now);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.currentPeriodEnd < :now")
    List<Subscription> findExpiredSubscriptions(@Param("now") OffsetDateTime now);
}
