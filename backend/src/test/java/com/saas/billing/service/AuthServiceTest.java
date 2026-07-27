package com.saas.billing.service;

import com.saas.billing.dto.AuthRequest;
import com.saas.billing.dto.AuthResponse;
import com.saas.billing.dto.RegisterRequest;
import com.saas.billing.entity.Organization;
import com.saas.billing.entity.OrganizationMember;
import com.saas.billing.entity.Plan;
import com.saas.billing.entity.Role;
import com.saas.billing.entity.Settings;
import com.saas.billing.entity.Subscription;
import com.saas.billing.entity.User;
import com.saas.billing.exception.BadRequestException;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.OrganizationMemberRepository;
import com.saas.billing.repository.OrganizationRepository;
import com.saas.billing.repository.PlanRepository;
import com.saas.billing.repository.RoleRepository;
import com.saas.billing.repository.SettingsRepository;
import com.saas.billing.repository.SubscriptionRepository;
import com.saas.billing.repository.UserRepository;
import com.saas.billing.security.TokenProvider;
import com.saas.billing.service.NotificationService;
import com.saas.billing.serviceImpl.AuthServiceImpl;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private Role role;
    private Organization organization;
    private OrganizationMember member;
    private Plan basicPlan;
    private Subscription subscription;
    private Settings settings;
    private RegisterRequest registerRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .passwordHash("encoded-password")
                .firstName("John")
                .lastName("Doe")
                .emailVerified(false)
                .verificationToken("verify-token")
                .isActive(true)
                .build();

        role = Role.builder()
        .id(1)
        .name("ROLE_ORGANIZATION")
        .build();

        organization = Organization.builder()
                .id(UUID.randomUUID())
                .name("Test Organization")
                .slug("test-organization")
                .build();

        member = OrganizationMember.builder()
                .organization(organization)
                .user(user)
                .role(role)
                .build();

        basicPlan = Plan.builder()
        .id(UUID.randomUUID())
        .name("Basic Plan")
        .amount(BigDecimal.ZERO)
        .currency("USD")
        .billingInterval("monthly")
        .trialPeriodDays(14)
        .isActive(true)
        .build();

        subscription = Subscription.builder()
                .organization(organization)
                .plan(basicPlan)
                .status("TRIAL")
                .currentPeriodStart(OffsetDateTime.now())
                .currentPeriodEnd(OffsetDateTime.now().plusDays(14))
                .trialStart(OffsetDateTime.now())
                .trialEnd(OffsetDateTime.now().plusDays(14))
                .cancelAtPeriodEnd(false)
                .build();

        settings = Settings.builder()
                .organization(organization)
                .billingEmail(user.getEmail())
                .country("US")
                .currency("USD")
                .build();

        registerRequest = new RegisterRequest(
                "john@example.com",
                "password123",
                "John",
                "Doe",
                "Test Organization"
        );

        authRequest = new AuthRequest(
                "john@example.com",
                "password123"
        );
    }    @Test
    void shouldLoginSuccessfully() {

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(tokenProvider.generateAccessToken(authentication))
                .thenReturn("access-token");

        when(tokenProvider.generateRefreshToken(authentication))
                .thenReturn("refresh-token");

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(organizationMemberRepository.findByUserId(user.getId()))
                .thenReturn(List.of(member));

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(organization.getId(), response.getOrganizationId());
        assertEquals(organization.getName(), response.getOrganizationName());
        assertEquals(organization.getSlug(), response.getOrganizationSlug());
        assertEquals("ROLE_ORGANIZATION", response.getRole());

        verify(authenticationManager).authenticate(any());
        verify(tokenProvider).generateAccessToken(authentication);
        verify(tokenProvider).generateRefreshToken(authentication);
        verify(userRepository).findByEmail(user.getEmail());
        verify(organizationMemberRepository).findByUserId(user.getId());
    }

    @Test
    void shouldLoginAsAdminWithoutOrganization() {

        user.setEmail("admin@saas.com");

        AuthRequest adminRequest = new AuthRequest(
                "admin@saas.com",
                "password123"
        );

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(tokenProvider.generateAccessToken(authentication))
                .thenReturn("access-token");

        when(tokenProvider.generateRefreshToken(authentication))
                .thenReturn("refresh-token");

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(organizationMemberRepository.findByUserId(user.getId()))
                .thenReturn(List.of());

        AuthResponse response = authService.login(adminRequest);

        assertNotNull(response);
        assertEquals("ROLE_ADMIN", response.getRole());
        assertNull(response.getOrganizationId());
        assertNull(response.getOrganizationName());
        assertNull(response.getOrganizationSlug());

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistDuringLogin() {

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(tokenProvider.generateAccessToken(authentication))
                .thenReturn("access-token");

        when(tokenProvider.generateRefreshToken(authentication))
                .thenReturn("refresh-token");

        when(userRepository.findByEmail(authRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.login(authRequest)
        );

        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail(authRequest.getEmail());
    }    @Test
    void shouldRegisterSuccessfully() {

        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    u.setId(user.getId());
                    return u;
                });

        when(organizationRepository.existsBySlug(anyString()))
                .thenReturn(false);

        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(invocation -> {
                    Organization org = invocation.getArgument(0);
                    org.setId(organization.getId());
                    return org;
                });

        when(roleRepository.findByName("ROLE_ORGANIZATION"))
                .thenReturn(Optional.of(role));

        when(planRepository.findAll())
                .thenReturn(List.of(basicPlan));

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(settingsRepository.save(any(Settings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(tokenProvider.generateAccessToken(authentication))
                .thenReturn("access-token");

        when(tokenProvider.generateRefreshToken(authentication))
                .thenReturn("refresh-token");

        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.of(user));

        when(organizationMemberRepository.findByUserId(user.getId()))
                .thenReturn(List.of(member));

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals(user.getEmail(), response.getEmail());

        verify(userRepository).save(any(User.class));
        verify(organizationRepository).save(any(Organization.class));
        verify(organizationMemberRepository).save(any(OrganizationMember.class));
        verify(settingsRepository).save(any(Settings.class));
        verify(subscriptionRepository).save(any(Subscription.class));

        verify(notificationService).sendEmail(
                anyString(),
                contains("Verify"),
                anyString()
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> authService.register(registerRequest)
        );

        verify(userRepository, never()).save(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenRoleDoesNotExist() {

        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(organizationRepository.existsBySlug(anyString()))
                .thenReturn(false);

        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(roleRepository.findByName("ROLE_ORGANIZATION"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.register(registerRequest)
        );
    }

    @Test
    void shouldThrowExceptionWhenDefaultPlanDoesNotExist() {

        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(organizationRepository.existsBySlug(anyString()))
                .thenReturn(false);

        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(roleRepository.findByName("ROLE_ORGANIZATION"))
                .thenReturn(Optional.of(role));

        when(planRepository.findAll())
                .thenReturn(List.of());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.register(registerRequest)
        );
    }    @Test
    void shouldVerifyEmailSuccessfully() {

        user.setVerificationToken("verify-token");

        when(userRepository.findByVerificationToken("verify-token"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.verifyEmail("verify-token");

        assertTrue(user.getEmailVerified());
        assertNull(user.getVerificationToken());

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenVerificationTokenIsInvalid() {

        when(userRepository.findByVerificationToken("invalid-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> authService.verifyEmail("invalid-token")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldGeneratePasswordResetTokenSuccessfully() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.forgotPassword(user.getEmail());

        assertNotNull(user.getPasswordResetToken());

        verify(userRepository).save(user);

        verify(notificationService).sendEmail(
                eq(user.getEmail()),
                contains("Password Reset"),
                contains("reset-password")
        );
    }

    @Test
    void shouldThrowExceptionWhenForgotPasswordUserDoesNotExist() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.forgotPassword(user.getEmail())
        );

        verify(notificationService, never())
                .sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void shouldResetPasswordSuccessfully() {

        user.setPasswordResetToken("reset-token");

        when(userRepository.findByPasswordResetToken("reset-token"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("new-password"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword(
                "reset-token",
                "new-password"
        );

        assertEquals("encoded-password", user.getPasswordHash());
        assertNull(user.getPasswordResetToken());

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenResetTokenIsInvalid() {

        when(userRepository.findByPasswordResetToken("bad-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> authService.resetPassword(
                        "bad-token",
                        "new-password"
                )
        );

        verify(userRepository, never()).save(any());
    }    @Test
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {

        when(tokenProvider.validateToken("invalid-refresh-token"))
                .thenReturn(false);

        assertThrows(
                BadRequestException.class,
                () -> authService.refreshToken("invalid-refresh-token")
        );

        verify(tokenProvider).validateToken("invalid-refresh-token");
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {

        when(tokenProvider.validateToken("refresh-token"))
                .thenReturn(true);

        when(tokenProvider.getUsernameFromToken("refresh-token"))
                .thenReturn(user.getEmail());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(organizationMemberRepository.findByUserId(user.getId()))
                .thenReturn(List.of(member));

        when(tokenProvider.generateAccessToken(any()))
                .thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken("refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getId(), response.getUserId());
    }

}