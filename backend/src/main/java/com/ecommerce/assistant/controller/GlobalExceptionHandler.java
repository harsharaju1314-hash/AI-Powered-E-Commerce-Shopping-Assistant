package com.ecommerce.assistant.controller;

import com.ecommerce.assistant.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse err = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        ErrorResponse err = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        ErrorResponse err = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        ErrorResponse err = new ErrorResponse(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(InventoryShortageException.class)
    public ResponseEntity<ErrorResponse> handleInventoryShortage(InventoryShortageException ex, HttpServletRequest request) {
        ErrorResponse err = new ErrorResponse(HttpStatus.CONFLICT.value(), "Inventory Shortage", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(ApprovalExpiredException.class)
    public ResponseEntity<ErrorResponse> handleApprovalExpired(ApprovalExpiredException ex, HttpServletRequest request) {
        ErrorResponse err = new ErrorResponse(HttpStatus.GONE.value(), "Approval Expired", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.GONE).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponse err = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Failed", "One or more input fields are invalid", request.getRequestURI(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        ErrorResponse err = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
