package com.saas.billing.service;

import com.saas.billing.dto.*;
import com.saas.billing.entity.Settings;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    OrganizationDto getOrganizationById(UUID organizationId);
    OrganizationDto updateOrganization(UUID organizationId, OrganizationDto dto);
    List<UserDto> getMembers(UUID organizationId);
    void addMember(UUID organizationId, String email, String roleName);
    void removeMember(UUID organizationId, UUID userId);
    Settings getSettings(UUID organizationId);
    Settings updateSettings(UUID organizationId, Settings settings);
}
