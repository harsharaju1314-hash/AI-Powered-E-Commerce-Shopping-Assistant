package com.ecommerce.assistant.exception;

public class ApprovalExpiredException extends RuntimeException {
    public ApprovalExpiredException(String message) {
        super(message);
    }
}
