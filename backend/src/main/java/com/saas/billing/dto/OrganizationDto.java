package com.saas.billing.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDto {
    private UUID id;
    private String name;
    private String slug;
}
