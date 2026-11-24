package com.ecommerce.assistant.exception;

public class InventoryShortageException extends RuntimeException {
    public InventoryShortageException(String message) {
        super(message);
    }
}
