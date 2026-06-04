package com.saas.billing.repository;

import com.saas.billing.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, UUID> {
    Optional<Settings> findByOrganizationId(UUID organizationId);
}
