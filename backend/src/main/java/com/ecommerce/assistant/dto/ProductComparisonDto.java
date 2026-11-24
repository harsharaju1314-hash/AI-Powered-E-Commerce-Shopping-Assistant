package com.ecommerce.assistant.dto;

import java.util.List;
import java.util.Map;

public class ProductComparisonDto {
    private List<ProductDto> products;
    private Map<String, List<String>> specificationMatrix;
    private String aiSummary;

    public ProductComparisonDto() {}

    public ProductComparisonDto(List<ProductDto> products, Map<String, List<String>> matrix, String aiSummary) {
        this.products = products;
        this.specificationMatrix = matrix;
        this.aiSummary = aiSummary;
    }

    public List<ProductDto> getProducts() { return products; }
    public void setProducts(List<ProductDto> products) { this.products = products; }
    public Map<String, List<String>> getSpecificationMatrix() { return specificationMatrix; }
    public void setSpecificationMatrix(Map<String, List<String>> specificationMatrix) { this.specificationMatrix = specificationMatrix; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
}
