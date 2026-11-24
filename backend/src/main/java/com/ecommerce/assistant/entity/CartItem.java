package com.ecommerce.assistant.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private ShoppingCart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public CartItem() {}

    public CartItem(ShoppingCart cart, Product product, Integer quantity, BigDecimal unitPrice) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ShoppingCart getCart() { return cart; }
    public void setCart(ShoppingCart cart) { this.cart = cart; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
