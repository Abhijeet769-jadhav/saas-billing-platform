package com.saas.billing.controller;

import com.saas.billing.dto.SupportTicketDto;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuthentication")
@Tag(name = "Support Tickets", description = "Endpoints for user helpdesk tickets submission and resolution")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    @Operation(summary = "Submit a new customer support ticket")
    public ResponseEntity<SupportTicketDto> createTicket(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SupportTicketDto dto) {
        return ResponseEntity.ok(supportTicketService.createTicket(userDetails.getOrganizationId(), userDetails.getId(), dto));
    }

    @GetMapping
    @Operation(summary = "List all customer support tickets for organization workspace")
    public ResponseEntity<List<SupportTicketDto>> getTickets(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(supportTicketService.getTickets(userDetails.getOrganizationId()));
    }

    @GetMapping("/all")
    @Operation(summary = "List all support tickets across the platform (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupportTicketDto>> getAllTickets() {
        return ResponseEntity.ok(supportTicketService.getAllTickets());
    }

    @PostMapping("/{ticketId}/resolve")
    @Operation(summary = "Resolve open support ticket status (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupportTicketDto> resolveTicket(@PathVariable("ticketId") UUID ticketId) {
        return ResponseEntity.ok(supportTicketService.resolveTicket(ticketId));
    }
}
