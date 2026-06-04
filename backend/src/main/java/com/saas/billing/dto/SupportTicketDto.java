package com.saas.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketDto {
    private UUID id;
    private UUID organizationId;
    private UUID userId;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private OffsetDateTime createdAt;
}
