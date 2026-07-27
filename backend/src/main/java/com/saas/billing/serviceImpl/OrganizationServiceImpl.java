package com.saas.billing.serviceImpl;

import com.saas.billing.dto.*;
import com.saas.billing.entity.*;
import com.saas.billing.exception.BadRequestException;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.mapper.DtoMapper;
import com.saas.billing.repository.*;
import com.saas.billing.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SettingsRepository settingsRepository;

    @Override
    public OrganizationDto getOrganizationById(UUID organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        return DtoMapper.toOrganizationDto(org);
    }

    @Override
    public OrganizationDto updateOrganization(UUID organizationId, OrganizationDto dto) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        org.setName(dto.getName());

        Organization saved = organizationRepository.save(org);
        return DtoMapper.toOrganizationDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getMembers(UUID organizationId) {
        return organizationMemberRepository.findByOrganizationId(organizationId)
                .stream()
                .map(member -> DtoMapper.toUserDto(member.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMember(UUID organizationId, String email, String roleName) {

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with email " + email + " not found"));

        if (organizationMemberRepository.existsByOrganizationIdAndUserId(
                organizationId, user.getId())) {
            throw new BadRequestException("User is already a member of this organization");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        OrganizationMember member = OrganizationMember.builder()
                .organization(org)
                .user(user)
                .role(role)
                .build();

        organizationMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMember(UUID organizationId, UUID userId) {

        OrganizationMember member = organizationMemberRepository
                .findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Membership not found"));

        organizationMemberRepository.delete(member);
    }

    @Override
    public Settings getSettings(UUID organizationId) {
        return settingsRepository.findByOrganizationId(organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Settings not found for this organization"));
    }

    @Override
    public Settings updateSettings(UUID organizationId, Settings settings) {

        Settings dbSettings = settingsRepository.findByOrganizationId(organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Settings not found"));

        dbSettings.setTaxRegistrationNumber(settings.getTaxRegistrationNumber());
        dbSettings.setGstin(settings.getGstin());
        dbSettings.setBillingEmail(settings.getBillingEmail());
        dbSettings.setCountry(settings.getCountry());
        dbSettings.setCurrency(settings.getCurrency());

        return settingsRepository.save(dbSettings);
    }
}