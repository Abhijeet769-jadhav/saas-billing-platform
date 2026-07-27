package com.saas.billing.service;

import com.saas.billing.dto.OrganizationDto;
import com.saas.billing.dto.UserDto;
import com.saas.billing.entity.Organization;
import com.saas.billing.entity.OrganizationMember;
import com.saas.billing.entity.Role;
import com.saas.billing.entity.Settings;
import com.saas.billing.entity.User;
import com.saas.billing.exception.BadRequestException;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.OrganizationMemberRepository;
import com.saas.billing.repository.OrganizationRepository;
import com.saas.billing.repository.RoleRepository;
import com.saas.billing.repository.SettingsRepository;
import com.saas.billing.repository.UserRepository;
import com.saas.billing.serviceImpl.OrganizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SettingsRepository settingsRepository;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private UUID organizationId;
    private UUID userId;

    private Organization organization;
    private OrganizationDto organizationDto;

    private User user;
    private Role role;
    private OrganizationMember organizationMember;
    private Settings settings;

    @BeforeEach
    void setUp() {

        organizationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        organization = Organization.builder()
                .id(organizationId)
                .name("Test Organization")
                .build();

        organizationDto = new OrganizationDto();
        organizationDto.setId(organizationId);
        organizationDto.setName("Updated Organization");

        user = User.builder()
                .id(userId)
                .email("test@test.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        role = Role.builder()
        .id(1)
        .name("ADMIN")
        .build();

        organizationMember = OrganizationMember.builder()
                .organization(organization)
                .user(user)
                .role(role)
                .build();

        settings = Settings.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .billingEmail("billing@test.com")
                .country("India")
                .currency("INR")
                .gstin("GST123456")
                .taxRegistrationNumber("TAX123")
                .build();
    }

    @Test
    void shouldGetOrganizationByIdSuccessfully() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        OrganizationDto result =
                organizationService.getOrganizationById(organizationId);

        assertNotNull(result);
        assertEquals(organizationId, result.getId());
        assertEquals("Test Organization", result.getName());

        verify(organizationRepository).findById(organizationId);
    }

    @Test
    void shouldThrowExceptionWhenOrganizationNotFound() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.getOrganizationById(organizationId)
        );

        verify(organizationRepository).findById(organizationId);
    }

    @Test
    void shouldUpdateOrganizationSuccessfully() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrganizationDto result =
                organizationService.updateOrganization(
                        organizationId,
                        organizationDto
                );

        assertNotNull(result);
        assertEquals("Updated Organization", result.getName());

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void shouldThrowWhenUpdatingOrganizationNotFound() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.updateOrganization(
                        organizationId,
                        organizationDto
                )
        );

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository, never()).save(any());
    }
        @Test
    void shouldGetMembersSuccessfully() {

        when(organizationMemberRepository.findByOrganizationId(organizationId))
                .thenReturn(List.of(organizationMember));

        List<UserDto> members =
                organizationService.getMembers(organizationId);

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(user.getId(), members.get(0).getId());
        assertEquals(user.getEmail(), members.get(0).getEmail());

        verify(organizationMemberRepository)
                .findByOrganizationId(organizationId);
    }

    @Test
    void shouldReturnEmptyMemberList() {

        when(organizationMemberRepository.findByOrganizationId(organizationId))
                .thenReturn(List.of());

        List<UserDto> members =
                organizationService.getMembers(organizationId);

        assertNotNull(members);
        assertTrue(members.isEmpty());

        verify(organizationMemberRepository)
                .findByOrganizationId(organizationId);
    }

    @Test
    void shouldAddMemberSuccessfully() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByName("ADMIN"))
                .thenReturn(Optional.of(role));

        when(organizationMemberRepository
                .existsByOrganizationIdAndUserId(
                        organizationId,
                        userId))
                .thenReturn(false);

        when(organizationMemberRepository.save(any(OrganizationMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() ->
                organizationService.addMember(
                        organizationId,
                        user.getEmail(),
                        "ADMIN"));

        verify(organizationRepository).findById(organizationId);
        verify(userRepository).findByEmail(user.getEmail());
        verify(roleRepository).findByName("ADMIN");
        verify(organizationMemberRepository)
                .existsByOrganizationIdAndUserId(
                        organizationId,
                        userId);
        verify(organizationMemberRepository)
                .save(any(OrganizationMember.class));
    }

    @Test
    void shouldThrowWhenOrganizationMissingWhileAddingMember() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.addMember(
                        organizationId,
                        user.getEmail(),
                        "ADMIN")
        );

        verify(organizationRepository).findById(organizationId);
        verify(organizationMemberRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserNotFoundWhileAddingMember() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.addMember(
                        organizationId,
                        user.getEmail(),
                        "ADMIN")
        );

        verify(organizationRepository).findById(organizationId);
        verify(userRepository).findByEmail(user.getEmail());
        verify(organizationMemberRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenRoleNotFoundWhileAddingMember() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByName("ADMIN"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.addMember(
                        organizationId,
                        user.getEmail(),
                        "ADMIN")
        );

        verify(roleRepository).findByName("ADMIN");
        verify(organizationMemberRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenMemberAlreadyExists() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
                
        when(organizationMemberRepository
                .existsByOrganizationIdAndUserId(
                        organizationId,
                        userId))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> organizationService.addMember(
                        organizationId,
                        user.getEmail(),
                        "ADMIN")
        );

        verify(organizationMemberRepository, never())
                .save(any());
    }
        @Test
    void shouldRemoveMemberSuccessfully() {

        when(organizationMemberRepository
                .findByOrganizationIdAndUserId(
                        organizationId,
                        userId))
                .thenReturn(Optional.of(organizationMember));

        doNothing().when(organizationMemberRepository)
                .delete(organizationMember);

        assertDoesNotThrow(() ->
                organizationService.removeMember(
                        organizationId,
                        userId));

        verify(organizationMemberRepository)
                .findByOrganizationIdAndUserId(
                        organizationId,
                        userId);

        verify(organizationMemberRepository)
                .delete(organizationMember);
    }

    @Test
    void shouldThrowWhenRemovingMemberNotFound() {

        when(organizationMemberRepository
                .findByOrganizationIdAndUserId(
                        organizationId,
                        userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.removeMember(
                        organizationId,
                        userId)
        );

        verify(organizationMemberRepository)
                .findByOrganizationIdAndUserId(
                        organizationId,
                        userId);

        verify(organizationMemberRepository, never())
                .delete(any());
    }

    @Test
    void shouldGetSettingsSuccessfully() {

        when(settingsRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(settings));

        Settings result =
                organizationService.getSettings(organizationId);

        assertNotNull(result);
        assertEquals(settings.getBillingEmail(), result.getBillingEmail());
        assertEquals(settings.getCountry(), result.getCountry());
        assertEquals(settings.getCurrency(), result.getCurrency());

        verify(settingsRepository)
                .findByOrganizationId(organizationId);
    }

    @Test
    void shouldThrowWhenSettingsNotFound() {

        when(settingsRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.getSettings(organizationId)
        );

        verify(settingsRepository)
                .findByOrganizationId(organizationId);
    }

    @Test
    void shouldUpdateSettingsSuccessfully() {

        when(settingsRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(settings));

        when(settingsRepository.save(any(Settings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Settings result =
                organizationService.updateSettings(
                        organizationId,
                        settings);

        assertNotNull(result);
        assertEquals(settings.getBillingEmail(), result.getBillingEmail());
        assertEquals(settings.getCountry(), result.getCountry());
        assertEquals(settings.getCurrency(), result.getCurrency());
        assertEquals(settings.getGstin(), result.getGstin());
        assertEquals(settings.getTaxRegistrationNumber(),
                result.getTaxRegistrationNumber());

        verify(settingsRepository)
                .findByOrganizationId(organizationId);

        verify(settingsRepository)
                .save(any(Settings.class));
    }

    @Test
    void shouldThrowWhenUpdatingSettingsNotFound() {

        when(settingsRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.updateSettings(
                        organizationId,
                        settings)
        );

        verify(settingsRepository)
                .findByOrganizationId(organizationId);

        verify(settingsRepository, never())
                .save(any());
    }
}