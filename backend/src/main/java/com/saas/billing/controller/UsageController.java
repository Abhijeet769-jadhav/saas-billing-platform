package com.saas.billing.controller;

import com.saas.billing.dto.UsageDto;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.service.UsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuthentication")
@Tag(name = "Usage Tracking", description = "Endpoints for tracking feature usage logs and limit checks")
public class UsageController {

    private final UsageService usageService;

    @PostMapping("/log")
    @Operation(summary = "Log new feature utilization event quantity")
    @PreAuthorize("hasAnyRole('ORGANIZATION', 'USER')")
    public ResponseEntity<String> logUsage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("metricKey") String metricKey,
            @RequestParam("quantity") int quantity) {
        usageService.logUsage(userDetails.getOrganizationId(), metricKey, quantity);
        return ResponseEntity.ok("Usage logged successfully");
    }

    @GetMapping("/current")
    @Operation(summary = "Get current usage quantities against limits for the active plan")
    public ResponseEntity<List<UsageDto>> getCurrentUsage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(usageService.getCurrentUsage(userDetails.getOrganizationId()));
    }

    @GetMapping("/check")
    @Operation(summary = "Perform a quick check if a feature limit is reached")
    public ResponseEntity<Boolean> checkLimit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("metricKey") String metricKey,
            @RequestParam(value = "increment", defaultValue = "1") int increment) {
        boolean underLimit = usageService.checkLimit(userDetails.getOrganizationId(), metricKey, increment);
        return ResponseEntity.ok(underLimit);
    }
}
