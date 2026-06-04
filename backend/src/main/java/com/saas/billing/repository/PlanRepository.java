package com.saas.billing.repository;

import com.saas.billing.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    List<Plan> findByIsActiveTrue();
    Optional<Plan> findByStripePriceId(String stripePriceId);
    Optional<Plan> findByStripeProductId(String stripeProductId);
}
