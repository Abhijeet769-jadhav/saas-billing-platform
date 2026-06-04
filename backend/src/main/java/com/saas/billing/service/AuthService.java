package com.saas.billing.service;

import com.saas.billing.dto.*;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    AuthResponse register(RegisterRequest request);
    void verifyEmail(String token);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    AuthResponse refreshToken(String refreshToken);
}
