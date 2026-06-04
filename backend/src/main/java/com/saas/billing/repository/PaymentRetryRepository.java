package com.saas.billing.repository;

import com.saas.billing.entity.PaymentRetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRetryRepository extends JpaRepository<PaymentRetry, UUID> {
    List<PaymentRetry> findByStatus(String status);

    @Query("SELECT p FROM PaymentRetry p WHERE p.status = 'PENDING' AND p.nextRetryAt <= :now")
    List<PaymentRetry> findPendingRetriesBefore(@Param("now") OffsetDateTime now);
}
