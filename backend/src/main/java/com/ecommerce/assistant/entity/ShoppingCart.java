package com.ecommerce.assistant.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 50)
    private String status = "ACTIVE";

    @Column(name = "discount_code", length = 50)
    private String discountCode;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ShoppingCart() {}

    public ShoppingCart(User user) {
        this.user = user;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public BigDecimal calculateSubtotal() {
        return items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotal() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal total = subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
