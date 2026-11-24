package com.ecommerce.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class ApplyDiscountRequest {
    @NotBlank(message = "Discount code is required")
    private String discountCode;

    public ApplyDiscountRequest() {}
    public ApplyDiscountRequest(String discountCode) { this.discountCode = discountCode; }
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
}
