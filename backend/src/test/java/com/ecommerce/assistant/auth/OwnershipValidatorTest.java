package com.ecommerce.assistant.auth;

import com.ecommerce.assistant.exception.UnauthorizedException;
import com.ecommerce.assistant.security.OwnershipValidator;
import com.ecommerce.assistant.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OwnershipValidatorTest {

    private OwnershipValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OwnershipValidator();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should permit resource access when user ID matches resource owner")
    void testValidateOwnershipMatches() {
        UserPrincipal principal = new UserPrincipal(42L, "user@test.com", "pass", "User", true,
            List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertDoesNotThrow(() -> validator.validateOwnership(42L));
        assertEquals(42L, validator.getCurrentUserId());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when customer accesses another user's resource")
    void testValidateOwnershipDifferentCustomerThrowsUnauthorized() {
        UserPrincipal principal = new UserPrincipal(42L, "user@test.com", "pass", "User", true,
            List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertThrows(UnauthorizedException.class, () -> validator.validateOwnership(99L));
    }

    @Test
    @DisplayName("Should allow ADMIN role to access any resource")
    void testValidateOwnershipAdminAllowsAll() {
        UserPrincipal admin = new UserPrincipal(1L, "admin@test.com", "pass", "Admin", true,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities())
        );

        assertDoesNotThrow(() -> validator.validateOwnership(99L));
    }
}
