package com.ecommerce.assistant.auth;

import com.ecommerce.assistant.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, 3600000, 86400000);
    }

    @Test
    @DisplayName("Should generate valid access token and extract subject")
    void testGenerateAccessTokenAndValidate() {
        String token = tokenProvider.generateAccessToken(15L, "alice@example.com", List.of("ROLE_CUSTOMER"));

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("alice@example.com", tokenProvider.getEmailFromToken(token));
        assertEquals(15L, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshTokenAndValidate() {
        String refreshToken = tokenProvider.generateRefreshToken(20L, "bob@example.com");

        assertNotNull(refreshToken);
        assertTrue(tokenProvider.validateToken(refreshToken));
        assertEquals("bob@example.com", tokenProvider.getEmailFromToken(refreshToken));
    }

    @Test
    @DisplayName("Should reject malformed or tampered token")
    void testValidateMalformedToken() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }
}
