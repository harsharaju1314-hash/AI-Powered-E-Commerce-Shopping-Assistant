package com.ecommerce.assistant.controller;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.security.OwnershipValidator;
import com.ecommerce.assistant.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for registration, login, token refresh, and user profile")
public class AuthController {

    private final AuthService authService;
    private final OwnershipValidator ownershipValidator;

    public AuthController(AuthService authService, OwnershipValidator ownershipValidator) {
        this.authService = authService;
        this.ownershipValidator = ownershipValidator;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh expired access token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get authenticated user profile and shopping preferences")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(authService.getProfile(userId));
    }
}
