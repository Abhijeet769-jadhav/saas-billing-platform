package com.saas.billing.controller;

import com.saas.billing.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test(@AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return "User is NULL";
        }

        return "SUCCESS\n\n"
                + "Email: " + user.getUsername() + "\n"
                + "Organization ID: " + user.getOrganizationId() + "\n"
                + "Authorities: " + user.getAuthorities();
    }
}