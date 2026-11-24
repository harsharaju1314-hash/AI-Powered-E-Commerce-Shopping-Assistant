package com.ecommerce.assistant.product;

import com.ecommerce.assistant.FakeRepositories.*;
import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.*;
import com.ecommerce.assistant.exception.ResourceNotFoundException;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.repository.ProductCategoryRepository;
import com.ecommerce.assistant.service.ProductService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private FakeProductRepository productRepository;
    private FakeProductCategoryRepository categoryRepository;
    private FakeInventoryRepository inventoryRepository;
    private FakeUserPreferenceRepository preferenceRepository;
    private MetricsService metricsService;
    private ProductService productService;

    private Product product1;
    private Product product2;
    private ProductCategory category;

    @BeforeEach
    void setUp() {
        productRepository = new FakeProductRepository();
        categoryRepository = new FakeProductCategoryRepository();
        inventoryRepository = new FakeInventoryRepository();
        preferenceRepository = new FakeUserPreferenceRepository();
        metricsService = new MetricsService(new SimpleMeterRegistry());

        category = new ProductCategory("Electronics", "electronics", "High-tech audio", "cpu");
        category.setId(1L);
        categoryRepository.save(category);

        productService = new ProductService(
            productRepository,
            categoryRepository,
            inventoryRepository,
            preferenceRepository,
            metricsService
        );

        product1 = new Product();
        product1.setId(101L);
        product1.setSku("ELEC-101");
        product1.setName("Wireless Noise-Canceling Headphones");
        product1.setSlug("wireless-headphones-101");
        product1.setCategory(category);
        product1.setBrand("AuraSound");
        product1.setPrice(BigDecimal.valueOf(149.99));
        product1.setRating(4.8);
        product1.setReviewCount(120);
        product1.setDescription("Premium wireless noise cancelling audio gear.");
        product1.setIsAvailable(true);
        productRepository.save(product1);

        inventoryRepository.save(new Inventory(product1, 50));

        product2 = new Product();
        product2.setId(102L);
        product2.setSku("ELEC-102");
        product2.setName("Compact Power Bank 65W");
        product2.setSlug("power-bank-102");
        product2.setCategory(category);
        product2.setBrand("NovaTech");
        product2.setPrice(BigDecimal.valueOf(49.99));
        product2.setRating(4.5);
        product2.setReviewCount(85);
        product2.setDescription("Fast charging USB-C PD external battery.");
        product2.setIsAvailable(true);
        productRepository.save(product2);

        inventoryRepository.save(new Inventory(product2, 80));
    }

    @Test
    @DisplayName("Should retrieve product details by ID with stock inventory")
    void testGetProductByIdSuccess() {
        ProductDto dto = productService.getProductById(101L);

        assertNotNull(dto);
        assertEquals(101L, dto.getId());
        assertEquals("Wireless Noise-Canceling Headphones", dto.getName());
        assertEquals(50, dto.getAvailableStock());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void testGetProductByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    @DisplayName("Should search and filter products by query and return paginated result")
    void testSearchProducts() {
        ProductSearchRequest request = new ProductSearchRequest();
        request.setQuery("headphones");
        request.setMinPrice(BigDecimal.valueOf(100));

        var result = productService.searchProducts(request);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Wireless Noise-Canceling Headphones", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Should generate side-by-side comparison matrix for selected product IDs")
    void testCompareProducts() {
        ProductComparisonDto comparison = productService.compareProducts(List.of(101L, 102L));

        assertNotNull(comparison);
        assertEquals(2, comparison.getProducts().size());
        assertTrue(comparison.getSpecificationMatrix().containsKey("Brand"));
        assertTrue(comparison.getSpecificationMatrix().containsKey("Price"));
        assertNotNull(comparison.getAiSummary());
    }

    @Test
    @DisplayName("Should return all product categories")
    void testGetAllCategories() {
        List<CategoryDto> categories = productService.getAllCategories();

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
    }
}
