package com.saas.billing.controller;

import com.saas.billing.dto.AnalyticsDto;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuthentication")
@Tag(name = "Analytics", description = "Endpoints for platform metrics, MRR, ARR, and CSV reports export")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/platform")
    @Operation(summary = "Get global billing analytics dashboard metrics (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsDto> getPlatformAnalytics() {
        return ResponseEntity.ok(analyticsService.getPlatformAnalytics());
    }

    @GetMapping("/workspace")
    @Operation(summary = "Get workspace specific billing analytics dashboard metrics")
    public ResponseEntity<AnalyticsDto> getWorkspaceAnalytics(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(analyticsService.getOrganizationAnalytics(userDetails.getOrganizationId()));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Export platform revenue log sheet as CSV (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> exportRevenueReport() {
        ByteArrayInputStream csvStream = analyticsService.exportRevenueReportCsv();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=revenue-report.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(csvStream));
    }
}
