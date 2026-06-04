package com.saas.billing.controller;

import com.saas.billing.dto.PlanDto;
import com.saas.billing.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuthentication")
@Tag(name = "Plans", description = "Endpoints for managing and querying subscription plans")
public class PlanController {

    private final PlanService planService;

    @GetMapping
    @Operation(summary = "Get list of all active subscription plans")
    public ResponseEntity<List<PlanDto>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details of a specific plan")
    public ResponseEntity<PlanDto> getPlanById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new subscription plan (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanDto> createPlan(@RequestBody PlanDto dto) {
        return ResponseEntity.ok(planService.createPlan(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing subscription plan (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanDto> updatePlan(@PathVariable("id") UUID id, @RequestBody PlanDto dto) {
        return ResponseEntity.ok(planService.updatePlan(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an existing plan (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deletePlan(@PathVariable("id") UUID id) {
        planService.deletePlan(id);
        return ResponseEntity.ok("Plan deleted successfully");
    }
}
