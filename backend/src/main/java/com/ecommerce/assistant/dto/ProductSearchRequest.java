package com.ecommerce.assistant.dto;

import java.math.BigDecimal;

public class ProductSearchRequest {
    private String query;
    private Long categoryId;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private Boolean availableOnly = true;
    private int page = 0;
    private int size = 20;
    private String sortBy = "rating";
    private String sortDirection = "desc";

    public ProductSearchRequest() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public Double getMinRating() { return minRating; }
    public void setMinRating(Double minRating) { this.minRating = minRating; }
    public Boolean getAvailableOnly() { return availableOnly; }
    public void setAvailableOnly(Boolean availableOnly) { this.availableOnly = availableOnly; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}
