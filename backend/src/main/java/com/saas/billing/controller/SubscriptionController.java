package com.saas.billing.controller;

import com.saas.billing.dto.*;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuthentication")
@Tag(name = "Subscriptions", description = "Endpoints for managing workspace subscriptions and plan transitions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @Operation(summary = "Get current subscription details for the workspace")
    public ResponseEntity<SubscriptionDto> getSubscription(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(subscriptionService.getSubscription(userDetails.getOrganizationId()));
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Create initial workspace subscription")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<SubscriptionDto> subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionChangeRequest request) {
        return ResponseEntity.ok(subscriptionService.subscribe(userDetails.getOrganizationId(), request));
    }

    @PostMapping("/upgrade")
    @Operation(summary = "Upgrade workspace plan tier")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<SubscriptionDto> upgrade(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionChangeRequest request) {
        return ResponseEntity.ok(subscriptionService.upgrade(userDetails.getOrganizationId(), request));
    }

    @PostMapping("/downgrade")
    @Operation(summary = "Downgrade workspace plan tier")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<SubscriptionDto> downgrade(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionChangeRequest request) {
        return ResponseEntity.ok(subscriptionService.downgrade(userDetails.getOrganizationId(), request));
    }

    @PostMapping("/pause")
    @Operation(summary = "Pause workspace active subscription")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> pause(@AuthenticationPrincipal CustomUserDetails userDetails) {
        subscriptionService.pause(userDetails.getOrganizationId());
        return ResponseEntity.ok("Subscription paused successfully");
    }

    @PostMapping("/resume")
    @Operation(summary = "Resume paused workspace subscription")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> resume(@AuthenticationPrincipal CustomUserDetails userDetails) {
        subscriptionService.resume(userDetails.getOrganizationId());
        return ResponseEntity.ok("Subscription resumed successfully");
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel active subscription at current period end")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> cancel(@AuthenticationPrincipal CustomUserDetails userDetails) {
        subscriptionService.cancel(userDetails.getOrganizationId());
        return ResponseEntity.ok("Subscription cancelled successfully");
    }
}
