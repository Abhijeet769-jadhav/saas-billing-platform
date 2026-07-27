package com.saas.billing.serviceImpl;

import com.saas.billing.dto.*;
import com.saas.billing.entity.*;
import com.saas.billing.exception.BadRequestException;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.*;
import com.saas.billing.security.TokenProvider;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.security.CustomUserDetailsService;
import com.saas.billing.service.AuthService;
import com.saas.billing.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final SettingsRepository settingsRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

   @Override
@Transactional(readOnly = true)
public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<OrganizationMember> memberships = organizationMemberRepository.findByUserId(user.getId());
        UUID orgId = null;
        String orgName = null;
        String orgSlug = null;
        String roleName = "ROLE_USER";

        if (!memberships.isEmpty()) {
            OrganizationMember member = memberships.get(0);
            orgId = member.getOrganization().getId();
            orgName = member.getOrganization().getName();
            orgSlug = member.getOrganization().getSlug();
            roleName = member.getRole().getName();
        } else if (user.getEmail().equalsIgnoreCase("admin@saas.com")) {
            roleName = "ROLE_ADMIN";
        }

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .organizationId(orgId)
                .organizationName(orgName)
                .organizationSlug(orgSlug)
                .role(roleName)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // Create new User
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Create new Organization
        String slug = request.getOrganizationName().toLowerCase().replaceAll("[^a-z0-9]", "-");
        if (organizationRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis() % 1000;
        }

        Organization org = Organization.builder()
                .name(request.getOrganizationName())
                .slug(slug)
                .build();
        Organization savedOrg = organizationRepository.save(org);

        // Fetch ROLE_ORGANIZATION role
        Role orgRole = roleRepository.findByName("ROLE_ORGANIZATION")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // Join user to organization as Owner/Organization Admin
        OrganizationMember member = OrganizationMember.builder()
                .organization(savedOrg)
                .user(savedUser)
                .role(orgRole)
                .build();
        organizationMemberRepository.save(member);

        // Create Settings
        Settings settings = Settings.builder()
                .organization(savedOrg)
                .country("US")
                .currency("USD")
                .billingEmail(savedUser.getEmail())
                .build();
        settingsRepository.save(settings);

        // Assign free Basic plan by default
        Plan basicPlan = planRepository.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains("basic"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Default plan not found"));

        Subscription sub = Subscription.builder()
                .organization(savedOrg)
                .plan(basicPlan)
                .status("TRIAL")
                .currentPeriodStart(OffsetDateTime.now())
                .currentPeriodEnd(OffsetDateTime.now().plusDays(14))
                .trialStart(OffsetDateTime.now())
                .trialEnd(OffsetDateTime.now().plusDays(14))
                .cancelAtPeriodEnd(false)
                .build();
        subscriptionRepository.save(sub);

        // Send email validation
        notificationService.sendEmail(
                savedUser.getEmail(),
                "Verify your SaaS Billing Workspace",
                "Hello " + savedUser.getFirstName() + ",\n\nClick link to verify email: "
                        + "http://localhost:3000/verify-email?token=" + savedUser.getVerificationToken()
        );

        // Log login and return tokens immediately
        return login(new AuthRequest(request.getEmail(), request.getPassword()));
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordResetToken(UUID.randomUUID().toString());
        userRepository.save(user);

        notificationService.sendEmail(
                user.getEmail(),
                "Password Reset Requested",
                "Reset your password by clicking here: "
                        + "http://localhost:3000/reset-password?token=" + user.getPasswordResetToken()
        );
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        userRepository.save(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (tokenProvider.validateToken(refreshToken)) {
            String email = tokenProvider.getUsernameFromToken(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Simulating authentications setup for token regeneration
            CustomUserDetails details = (CustomUserDetails) new CustomUserDetailsService(userRepository, organizationMemberRepository)
                    .loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
            String newAccessToken = tokenProvider.generateAccessToken(authentication);

            return AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(details.getAuthorities().iterator().next().getAuthority())
                    .build();
        }
        throw new BadRequestException("Invalid refresh token credentials");
    }
}
