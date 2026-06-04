package com.saas.billing.repository;

import com.saas.billing.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {
    List<UsageLog> findByOrganizationIdAndMetricKeyOrderByLoggedAtDesc(UUID organizationId, String metricKey);

    @Query("SELECT COALESCE(SUM(u.quantity), 0) FROM UsageLog u " +
           "WHERE u.organization.id = :organizationId " +
           "AND u.metricKey = :metricKey " +
           "AND u.loggedAt >= :since")
    int sumQuantityByOrganizationAndMetricSince(@Param("organizationId") UUID organizationId,
                                                @Param("metricKey") String metricKey,
                                                @Param("since") OffsetDateTime since);
}
