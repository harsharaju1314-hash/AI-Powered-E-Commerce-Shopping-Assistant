package com.ecommerce.assistant.controller;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Catalog", description = "Endpoints for catalog browsing, faceted filtering, search, and product comparisons")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List and filter products with pagination")
    public ResponseEntity<PageResponse<ProductDto>> listProducts(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Double minRating,
        @RequestParam(required = false, defaultValue = "true") Boolean availableOnly,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size,
        @RequestParam(required = false, defaultValue = "rating") String sortBy,
        @RequestParam(required = false, defaultValue = "desc") String sortDirection
    ) {
        ProductSearchRequest req = new ProductSearchRequest();
        req.setQuery(query);
        req.setCategoryId(categoryId);
        req.setBrand(brand);
        req.setMinPrice(minPrice);
        req.setMaxPrice(maxPrice);
        req.setMinRating(minRating);
        req.setAvailableOnly(availableOnly);
        req.setPage(page);
        req.setSize(size);
        req.setSortBy(sortBy);
        req.setSortDirection(sortDirection);

        return ResponseEntity.ok(productService.searchProducts(req));
    }

    @PostMapping("/search")
    @Operation(summary = "Execute advanced faceted product search")
    public ResponseEntity<PageResponse<ProductDto>> searchProducts(@RequestBody ProductSearchRequest request) {
        return ResponseEntity.ok(productService.searchProducts(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details by ID")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product details by URL slug")
    public ResponseEntity<ProductDto> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all product categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/brands")
    @Operation(summary = "Get all distinct product brands")
    public ResponseEntity<List<String>> getBrands() {
        return ResponseEntity.ok(productService.getAllBrands());
    }

    @PostMapping("/compare")
    @Operation(summary = "Generate side-by-side comparison matrix for multiple product IDs")
    public ResponseEntity<ProductComparisonDto> compareProducts(@RequestBody List<Long> productIds) {
        return ResponseEntity.ok(productService.compareProducts(productIds));
    }
}
