package com.saas.billing.security;

import com.saas.billing.entity.User;
import com.saas.billing.entity.OrganizationMember;
import com.saas.billing.repository.UserRepository;
import com.saas.billing.repository.OrganizationMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Load organization memberships to inject tenant info
        List<OrganizationMember> memberships = organizationMemberRepository.findByUserId(user.getId());

        UUID orgId = null;
        String orgSlug = null;
        String roleName = "ROLE_USER";

        if (!memberships.isEmpty()) {
            OrganizationMember activeMember = memberships.get(0); // Load first active membership as context
            orgId = activeMember.getOrganization().getId();
            orgSlug = activeMember.getOrganization().getSlug();
            roleName = activeMember.getRole().getName();
        } else {
            // Check if user is a global platform admin (no direct org membership but has ROLE_ADMIN role in database somehow)
            // For safety, fallback if no membership is mapped yet.
            if (user.getEmail().equalsIgnoreCase("admin@saas.com")) {
                roleName = "ROLE_ADMIN";
            }
        }

        return new CustomUserDetails(user, orgId, orgSlug, roleName);
    }
}
