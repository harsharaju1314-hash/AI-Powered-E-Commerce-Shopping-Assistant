package com.ecommerce.assistant.dto;

import java.math.BigDecimal;

public class ProductDto {
    private Long id;
    private String sku;
    private String name;
    private String slug;
    private Long categoryId;
    private String categoryName;
    private String brand;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Double rating;
    private Integer reviewCount;
    private String description;
    private String specifications;
    private String features;
    private String tags;
    private String imageUrl;
    private Boolean isAvailable;
    private Integer availableStock;

    public ProductDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
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
    @com.fasterxml.jackson.annotation.JsonProperty("image_url")
    public String getImageUrlSnake() { return imageUrl; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean available) { isAvailable = available; }
    @com.fasterxml.jackson.annotation.JsonProperty("is_available")
    public Boolean getIsAvailableSnake() { return isAvailable; }

    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    @com.fasterxml.jackson.annotation.JsonProperty("inventory_count")
    public Integer getInventoryCountSnake() { return availableStock != null ? availableStock : 50; }

    @com.fasterxml.jackson.annotation.JsonProperty("category_id")
    public Long getCategoryIdSnake() { return categoryId; }

    @com.fasterxml.jackson.annotation.JsonProperty("category_name")
    public String getCategoryNameSnake() { return categoryName; }

    @com.fasterxml.jackson.annotation.JsonProperty("original_price")
    public BigDecimal getOriginalPriceSnake() { return originalPrice; }

    @com.fasterxml.jackson.annotation.JsonProperty("review_count")
    public Integer getReviewCountSnake() { return reviewCount; }
}
