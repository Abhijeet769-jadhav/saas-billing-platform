package com.saas.billing.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private UUID id;
    private String title;
    private String message;
    private Boolean isRead;
    private OffsetDateTime createdAt;
}
