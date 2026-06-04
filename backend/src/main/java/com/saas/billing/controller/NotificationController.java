package com.saas.billing.controller;

import com.saas.billing.dto.NotificationDto;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuthentication")
@Tag(name = "Notifications", description = "Endpoints for checking in-app notifications and alerts")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get current notification alerts list for authenticated user")
    public ResponseEntity<List<NotificationDto>> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getNotifications(userDetails.getId()));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a specific notification alert as read")
    public ResponseEntity<String> markAsRead(@PathVariable("id") UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read successfully");
    }
}
