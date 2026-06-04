package com.saas.billing.controller;

import com.saas.billing.dto.PaymentDto;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Endpoints for processing checkout sessions, webhooks, and refunds")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    @SecurityRequirement(name = "BearerAuthentication")
    @Operation(summary = "Create Stripe Checkout session redirect URL")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("planId") UUID planId,
            @RequestParam("successUrl") String successUrl,
            @RequestParam("cancelUrl") String cancelUrl) {
        String sessionUrl = paymentService.createCheckoutSession(userDetails.getOrganizationId(), planId, successUrl, cancelUrl);
        return ResponseEntity.ok(sessionUrl);
    }

    @GetMapping("/history")
    @SecurityRequirement(name = "BearerAuthentication")
    @Operation(summary = "Get payment transaction history for organization")
    public ResponseEntity<List<PaymentDto>> getPaymentHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(userDetails.getOrganizationId()));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Public Stripe Webhook event processing gateway")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        paymentService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok("Webhook processed successfully");
    }

    @PostMapping("/refund/{paymentId}")
    @SecurityRequirement(name = "BearerAuthentication")
    @Operation(summary = "Refund a specific transaction (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> refund(@PathVariable("paymentId") UUID paymentId) {
        paymentService.refundPayment(paymentId);
        return ResponseEntity.ok("Payment refunded successfully");
    }

    @PostMapping("/retry/{invoiceId}")
    @SecurityRequirement(name = "BearerAuthentication")
    @Operation(summary = "Retry payment on a failed/open invoice")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> retry(@PathVariable("invoiceId") UUID invoiceId) {
        paymentService.retryPayment(invoiceId);
        return ResponseEntity.ok("Payment retry completed successfully");
    }
}
