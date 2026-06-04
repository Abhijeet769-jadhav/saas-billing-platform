package com.saas.billing.controller;

import com.saas.billing.dto.*;
import com.saas.billing.entity.Settings;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuthentication")
@Tag(name = "Organizations", description = "Endpoints for managing workspace organizations and settings")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/current")
    @Operation(summary = "Get current organization details")
    public ResponseEntity<OrganizationDto> getCurrentOrg(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(organizationService.getOrganizationById(userDetails.getOrganizationId()));
    }

    @PutMapping("/current")
    @Operation(summary = "Update current organization details")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<OrganizationDto> updateCurrentOrg(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OrganizationDto dto) {
        return ResponseEntity.ok(organizationService.updateOrganization(userDetails.getOrganizationId(), dto));
    }

    @GetMapping("/members")
    @Operation(summary = "List all workspace organization members")
    public ResponseEntity<List<UserDto>> getMembers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(organizationService.getMembers(userDetails.getOrganizationId()));
    }

    @PostMapping("/members")
    @Operation(summary = "Invite and add a member user to organization workspace")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> addMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("email") String email,
            @RequestParam("role") String role) {
        organizationService.addMember(userDetails.getOrganizationId(), email, role);
        return ResponseEntity.ok("Member user added successfully");
    }

    @DeleteMapping("/members/{userId}")
    @Operation(summary = "Remove user member from organization workspace")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> removeMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("userId") UUID userId) {
        organizationService.removeMember(userDetails.getOrganizationId(), userId);
        return ResponseEntity.ok("Member user removed successfully");
    }

    @GetMapping("/settings")
    @Operation(summary = "Get current organization billing settings")
    public ResponseEntity<Settings> getSettings(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(organizationService.getSettings(userDetails.getOrganizationId()));
    }

    @PutMapping("/settings")
    @Operation(summary = "Update organization billing settings (GSTIN, Tax info)")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Settings> updateSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Settings settings) {
        return ResponseEntity.ok(organizationService.updateSettings(userDetails.getOrganizationId(), settings));
    }
}
