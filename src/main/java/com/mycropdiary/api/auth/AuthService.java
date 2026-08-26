package com.mycropdiary.api.auth;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public void register(AuthController.RegisterRequest request) {
        // TODO Week 3: normalize email, check duplicate, hash password, send verification email.
        throw new UnsupportedOperationException("Registration implementation is assigned to Week 3");
    }

    public AuthController.TokenResponse login(AuthController.LoginRequest request) {
        // TODO Week 3: authenticate, enforce lock status, issue access/refresh tokens.
        throw new UnsupportedOperationException("Login implementation is assigned to Week 3");
    }
}
