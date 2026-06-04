package com.saas.billing.repository;

import com.saas.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId);
    List<Invoice> findByStatus(String status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.createdAt >= :startDate AND i.status = 'PAID'")
    long countPaidInvoicesSince(@Param("startDate") OffsetDateTime startDate);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.status = 'PAID'")
    double sumAllPaidRevenue();
}
