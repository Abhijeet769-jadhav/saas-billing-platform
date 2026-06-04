package com.saas.billing.controller;

import com.saas.billing.dto.*;
import com.saas.billing.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Register user and initialize new tenant organization")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify user email account with token")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send password reset link to registered email")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Password reset email sent successfully");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset account password with reset token")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token, @RequestParam("password") String password) {
        authService.resetPassword(token, password);
        return ResponseEntity.ok("Password has been reset successfully");
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Generate new access token from refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }
}
