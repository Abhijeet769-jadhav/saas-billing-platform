package com.saas.billing.repository;

import com.saas.billing.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    @Query("""
        SELECT om
        FROM OrganizationMember om
        JOIN FETCH om.user
        JOIN FETCH om.organization
        JOIN FETCH om.role
        WHERE om.organization.id = :organizationId
        """)
    List<OrganizationMember> findByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("""
        SELECT om
        FROM OrganizationMember om
        JOIN FETCH om.user
        JOIN FETCH om.organization
        JOIN FETCH om.role
        WHERE om.user.id = :userId
        """)
    List<OrganizationMember> findByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT om
        FROM OrganizationMember om
        JOIN FETCH om.user
        JOIN FETCH om.organization
        JOIN FETCH om.role
        WHERE om.organization.id = :organizationId
          AND om.user.id = :userId
        """)
    Optional<OrganizationMember> findByOrganizationIdAndUserId(
            @Param("organizationId") UUID organizationId,
            @Param("userId") UUID userId);

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);
}