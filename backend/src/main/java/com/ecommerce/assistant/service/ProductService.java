package com.ecommerce.assistant.service;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.Inventory;
import com.ecommerce.assistant.entity.Product;
import com.ecommerce.assistant.entity.ProductCategory;
import com.ecommerce.assistant.exception.ResourceNotFoundException;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.repository.InventoryRepository;
import com.ecommerce.assistant.repository.ProductCategoryRepository;
import com.ecommerce.assistant.repository.ProductRepository;
import com.ecommerce.assistant.repository.UserPreferenceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final MetricsService metricsService;

    public ProductService(
        ProductRepository productRepository,
        ProductCategoryRepository categoryRepository,
        InventoryRepository inventoryRepository,
        UserPreferenceRepository preferenceRepository,
        MetricsService metricsService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.preferenceRepository = preferenceRepository;
        this.metricsService = metricsService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> searchProducts(ProductSearchRequest req) {
        long start = System.currentTimeMillis();

        Sort sort = Sort.by(
            "asc".equalsIgnoreCase(req.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
            req.getSortBy() != null ? req.getSortBy() : "rating"
        );
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        Page<Product> page = productRepository.searchProducts(
            req.getQuery(),
            req.getCategoryId(),
            req.getBrand(),
            req.getMinPrice(),
            req.getMaxPrice(),
            req.getMinRating(),
            req.getAvailableOnly(),
            pageable
        );

        metricsService.recordDatabaseQueryTime(System.currentTimeMillis() - start);

        List<ProductDto> dtos = page.getContent().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());

        return new PageResponse<>(
            dtos,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }

    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        long start = System.currentTimeMillis();
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        metricsService.recordDatabaseQueryTime(System.currentTimeMillis() - start);
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return mapToDto(product);
    }

    @Cacheable(value = "categories")
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(c -> new CategoryDto(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getIcon()))
            .collect(Collectors.toList());
    }

    @Cacheable(value = "brands")
    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        return productRepository.findAll().stream()
            .map(Product::getBrand)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductComparisonDto compareProducts(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        List<ProductDto> dtos = products.stream().map(this::mapToDto).collect(Collectors.toList());

        Map<String, List<String>> matrix = new LinkedHashMap<>();
        matrix.put("Brand", dtos.stream().map(ProductDto::getBrand).collect(Collectors.toList()));
        matrix.put("Price", dtos.stream().map(p -> "$" + p.getPrice()).collect(Collectors.toList()));
        matrix.put("Rating", dtos.stream().map(p -> p.getRating() + " / 5.0 (" + p.getReviewCount() + " reviews)").collect(Collectors.toList()));
        matrix.put("Availability", dtos.stream().map(p -> p.getIsAvailable() ? "In Stock (" + p.getAvailableStock() + " units)" : "Out of Stock").collect(Collectors.toList()));

        StringBuilder summary = new StringBuilder();
        if (!dtos.isEmpty()) {
            ProductDto topRated = dtos.stream().max(Comparator.comparing(ProductDto::getRating)).orElse(dtos.get(0));
            ProductDto lowestPrice = dtos.stream().min(Comparator.comparing(ProductDto::getPrice)).orElse(dtos.get(0));
            summary.append("Comparison Summary: ");
            summary.append(topRated.getName()).append(" offers the highest customer rating (").append(topRated.getRating()).append("). ");
            summary.append(lowestPrice.getName()).append(" is the most budget-friendly option at $").append(lowestPrice.getPrice()).append(".");
        }

        return new ProductComparisonDto(dtos, matrix, summary.toString());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getRecommendedProducts(Long userId, int limit) {
        if (userId != null) {
            Optional<com.ecommerce.assistant.entity.UserPreference> prefOpt = preferenceRepository.findByUserId(userId);
            if (prefOpt.isPresent()) {
                com.ecommerce.assistant.entity.UserPreference pref = prefOpt.get();
                ProductSearchRequest req = new ProductSearchRequest();
                req.setSize(limit);
                req.setMaxPrice(pref.getMaxBudget());
                req.setMinRating(4.0);
                PageResponse<ProductDto> page = searchProducts(req);
                if (!page.getContent().isEmpty()) {
                    return page.getContent();
                }
            }
        }
        return productRepository.findTop10ByOrderByRatingDesc().stream()
            .limit(limit)
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    public ProductDto mapToDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setSku(p.getSku());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }
        dto.setBrand(p.getBrand());
        dto.setPrice(p.getPrice());
        dto.setOriginalPrice(p.getOriginalPrice());
        dto.setRating(p.getRating());
        dto.setReviewCount(p.getReviewCount());
        dto.setDescription(p.getDescription());
        dto.setSpecifications(p.getSpecifications());
        dto.setFeatures(p.getFeatures());
        dto.setTags(p.getTags());
        dto.setImageUrl(p.getImageUrl());
        dto.setIsAvailable(p.getIsAvailable());

        inventoryRepository.findByProductId(p.getId())
            .ifPresent(inv -> dto.setAvailableStock(inv.getAvailableQuantity()));

        return dto;
    }
}
