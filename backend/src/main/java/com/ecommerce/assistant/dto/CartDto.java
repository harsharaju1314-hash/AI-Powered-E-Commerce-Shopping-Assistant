package com.ecommerce.assistant.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDto {
    private Long id;
    private Long userId;
    private String status;
    private String discountCode;
    private BigDecimal discountAmount;
    private BigDecimal subtotal;
    private BigDecimal total;
    private List<CartItemDto> items = new ArrayList<>();

    public CartDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
}
