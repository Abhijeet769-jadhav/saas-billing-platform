package com.saas.billing.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UUID userId;
    private String email;
    private UUID organizationId;
    private String organizationName;
    private String organizationSlug;
    private String role;
}
