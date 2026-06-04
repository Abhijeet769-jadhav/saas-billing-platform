package com.saas.billing.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean emailVerified;
    private Boolean isActive;
}
