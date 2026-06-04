package com.saas.billing.repository;

import com.saas.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
