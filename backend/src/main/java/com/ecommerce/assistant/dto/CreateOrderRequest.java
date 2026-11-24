package com.ecommerce.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateOrderRequest {
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String idempotencyKey;

    public CreateOrderRequest() {}
    public CreateOrderRequest(String shippingAddress, String idempotencyKey) {
        this.shippingAddress = shippingAddress;
        this.idempotencyKey = idempotencyKey;
    }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
