package com.saas.billing.controller;

import com.saas.billing.dto.UserDto;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuthentication")
@Tag(name = "Users", description = "Endpoints for managing user accounts and profiles")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile info")
    public ResponseEntity<UserDto> updateCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.updateUser(userDetails.getId(), dto));
    }

    @DeleteMapping("/deactivate")
    @Operation(summary = "Deactivate current user account")
    public ResponseEntity<String> deactivateUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deactivateUser(userDetails.getId());
        return ResponseEntity.ok("Account deactivated successfully");
    }
}
