package com.ecommerce.assistant.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "preferred_categories", columnDefinition = "TEXT")
    private String preferredCategories;

    @Column(name = "preferred_brands", columnDefinition = "TEXT")
    private String preferredBrands;

    @Column(name = "max_budget", precision = 12, scale = 2)
    private BigDecimal maxBudget;

    @Column(name = "favorite_features", columnDefinition = "TEXT")
    private String favoriteFeatures;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserPreference() {}

    public UserPreference(User user) {
        this.user = user;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPreferredCategories() { return preferredCategories; }
    public void setPreferredCategories(String preferredCategories) { this.preferredCategories = preferredCategories; }

    public String getPreferredBrands() { return preferredBrands; }
    public void setPreferredBrands(String preferredBrands) { this.preferredBrands = preferredBrands; }

    public BigDecimal getMaxBudget() { return maxBudget; }
    public void setMaxBudget(BigDecimal maxBudget) { this.maxBudget = maxBudget; }

    public String getFavoriteFeatures() { return favoriteFeatures; }
    public void setFavoriteFeatures(String favoriteFeatures) { this.favoriteFeatures = favoriteFeatures; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
