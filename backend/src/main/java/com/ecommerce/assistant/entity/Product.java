package com.ecommerce.assistant.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(precision = 3, scale = 2)
    private Double rating = 0.0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    @Column(columnDefinition = "TEXT")
    private String features;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Product() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean available) { isAvailable = available; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
