package com.ecommerce.assistant.dto;

import java.math.BigDecimal;
import java.util.List;

public class UserProfileResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private List<String> roles;
    private List<String> preferredCategories;
    private List<String> preferredBrands;
    private BigDecimal maxBudget;
    private List<String> favoriteFeatures;

    public UserProfileResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getPreferredCategories() { return preferredCategories; }
    public void setPreferredCategories(List<String> preferredCategories) { this.preferredCategories = preferredCategories; }
    public List<String> getPreferredBrands() { return preferredBrands; }
    public void setPreferredBrands(List<String> preferredBrands) { this.preferredBrands = preferredBrands; }
    public BigDecimal getMaxBudget() { return maxBudget; }
    public void setMaxBudget(BigDecimal maxBudget) { this.maxBudget = maxBudget; }
    public List<String> getFavoriteFeatures() { return favoriteFeatures; }
    public void setFavoriteFeatures(List<String> favoriteFeatures) { this.favoriteFeatures = favoriteFeatures; }
}
