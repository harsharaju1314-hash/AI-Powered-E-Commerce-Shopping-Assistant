package com.ecommerce.assistant.auth;

import com.ecommerce.assistant.controller.GlobalExceptionHandler;
import com.ecommerce.assistant.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    @DisplayName("Should return 404 for ResourceNotFoundException")
    void testHandleNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
            new ResourceNotFoundException("Item not found"), request
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Item not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should return 400 for BadRequestException")
    void testHandleBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(
            new BadRequestException("Bad parameters"), request
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 401 for UnauthorizedException")
    void testHandleUnauthorized() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(
            new UnauthorizedException("Token missing"), request
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 409 for ConflictException and InventoryShortageException")
    void testHandleConflictAndShortage() {
        ResponseEntity<ErrorResponse> resConflict = handler.handleConflict(
            new ConflictException("Email exists"), request
        );
        assertEquals(HttpStatus.CONFLICT, resConflict.getStatusCode());

        ResponseEntity<ErrorResponse> resShortage = handler.handleInventoryShortage(
            new InventoryShortageException("Out of stock"), request
        );
        assertEquals(HttpStatus.CONFLICT, resShortage.getStatusCode());
    }
}
