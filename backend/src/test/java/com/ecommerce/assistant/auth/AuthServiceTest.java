package com.ecommerce.assistant.auth;

import com.ecommerce.assistant.FakeRepositories.*;
import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.Role;
import com.ecommerce.assistant.entity.User;
import com.ecommerce.assistant.exception.ConflictException;
import com.ecommerce.assistant.exception.UnauthorizedException;
import com.ecommerce.assistant.security.JwtTokenProvider;
import com.ecommerce.assistant.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private FakeUserRepository userRepository;
    private FakeRoleRepository roleRepository;
    private FakeUserPreferenceRepository preferenceRepository;
    private FakeShoppingCartRepository cartRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        roleRepository = new FakeRoleRepository();
        preferenceRepository = new FakeUserPreferenceRepository();
        cartRepository = new FakeShoppingCartRepository();

        passwordEncoder = new PasswordEncoder() {
            @Override public String encode(CharSequence raw) { return "hashed_" + raw; }
            @Override public boolean matches(CharSequence raw, String encoded) { return ("hashed_" + raw).equals(encoded); }
        };

        tokenProvider = new JwtTokenProvider("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 3600000, 86400000);

        authService = new AuthService(
            userRepository,
            roleRepository,
            preferenceRepository,
            cartRepository,
            passwordEncoder,
            tokenProvider
        );

        testUser = new User("test@example.com", "hashed_password123", "Test User");
        testUser.setId(10L);
        testUser.setRoles(Set.of(new Role("ROLE_CUSTOMER")));
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should successfully register new user with customer role")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password123", "New User");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("new@example.com", response.getEmail());
        assertTrue(userRepository.existsByEmail("new@example.com"));
    }

    @Test
    @DisplayName("Should throw ConflictException when registering duplicate email")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "User");

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should authenticate and return JWT on valid credentials")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals(10L, response.getUserId());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException on invalid password")
    void testLoginInvalidPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException for deactivated account")
    void testLoginDeactivatedAccount() {
        testUser.setIsActive(false);
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should refresh access token when refresh token is valid")
    void testRefreshTokenSuccess() {
        String refreshToken = tokenProvider.generateRefreshToken(10L, "test@example.com");
        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException on invalid refresh token")
    void testRefreshTokenInvalid() {
        TokenRefreshRequest request = new TokenRefreshRequest("invalid-token");

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));
    }

    @Test
    @DisplayName("Should retrieve user profile with roles")
    void testGetProfileSuccess() {
        UserProfileResponse profile = authService.getProfile(10L);

        assertNotNull(profile);
        assertEquals("test@example.com", profile.getEmail());
        assertTrue(profile.getRoles().contains("ROLE_CUSTOMER"));
    }
}
