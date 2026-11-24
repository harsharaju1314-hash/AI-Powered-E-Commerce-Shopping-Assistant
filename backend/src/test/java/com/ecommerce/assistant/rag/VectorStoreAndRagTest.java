package com.ecommerce.assistant.rag;

import com.ecommerce.assistant.FakeRepositories.*;
import com.ecommerce.assistant.dto.ProductDto;
import com.ecommerce.assistant.entity.Product;
import com.ecommerce.assistant.entity.ProductCategory;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.repository.ProductCategoryRepository;
import com.ecommerce.assistant.repository.ProductEmbeddingRepository;
import com.ecommerce.assistant.service.ProductService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VectorStoreAndRagTest {

    private FakeProductRepository productRepository;
    private FakeProductEmbeddingRepository embeddingRepository;
    private FakeProductCategoryRepository categoryRepository;
    private ProductService productService;
    private MetricsService metricsService;
    private EmbeddingModelClient embeddingClient;
    private VectorStoreService vectorStoreService;
    private ContextGroundingService groundingService;
    private RagEvaluationService evaluationService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        productRepository = new FakeProductRepository();
        FakeInventoryRepository inventoryRepository = new FakeInventoryRepository();
        FakeUserPreferenceRepository preferenceRepository = new FakeUserPreferenceRepository();
        categoryRepository = new FakeProductCategoryRepository();
        embeddingRepository = new FakeProductEmbeddingRepository();
        metricsService = new MetricsService(new SimpleMeterRegistry());
        embeddingClient = new EmbeddingModelClient("test-key", "text-embedding-3-small");

        productService = new ProductService(
            productRepository, categoryRepository, inventoryRepository, preferenceRepository, metricsService
        );

        vectorStoreService = new VectorStoreService(
            productRepository, embeddingRepository, embeddingClient, productService, metricsService
        );
        groundingService = new ContextGroundingService();
        evaluationService = new RagEvaluationService(vectorStoreService);

        ProductCategory cat = new ProductCategory("Electronics", "electronics", "desc", "icon");
        cat.setId(1L);

        product1 = new Product();
        product1.setId(101L);
        product1.setName("Noise Cancelling Wireless Headphones");
        product1.setBrand("AuraSound");
        product1.setCategory(cat);
        product1.setPrice(BigDecimal.valueOf(129.99));
        product1.setRating(4.9);
        product1.setDescription("Active noise cancelling high fidelity headphones.");
        product1.setIsAvailable(true);
        productRepository.save(product1);

        product2 = new Product();
        product2.setId(102L);
        product2.setName("Curved Gaming Monitor");
        product2.setBrand("NovaTech");
        product2.setCategory(cat);
        product2.setPrice(BigDecimal.valueOf(399.99));
        product2.setRating(4.6);
        product2.setDescription("Ultra high resolution curved gaming display.");
        product2.setIsAvailable(true);
        productRepository.save(product2);
    }

    @Test
    @DisplayName("Should generate normalized embeddings and compute cosine similarity")
    void testCosineSimilarityIdenticalAndDifferent() {
        float[] v1 = embeddingClient.generateEmbedding("wireless headphones audio");
        float[] v2 = embeddingClient.generateEmbedding("wireless headphones audio");
        float[] v3 = embeddingClient.generateEmbedding("kitchen air fryer ceramic");

        double simIdentical = EmbeddingModelClient.cosineSimilarity(v1, v2);
        double simDifferent = EmbeddingModelClient.cosineSimilarity(v1, v3);

        assertEquals(1.0, simIdentical, 0.001);
        assertTrue(simDifferent < simIdentical);
    }

    @Test
    @DisplayName("Should retrieve semantically relevant products with hybrid filtering")
    void testSimilaritySearchWithFilters() {
        List<ProductDto> results = vectorStoreService.similaritySearch(
            "headphones", 1L, BigDecimal.valueOf(200), 4.5, 5
        );

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("Noise Cancelling Wireless Headphones", results.get(0).getName());
    }

    @Test
    @DisplayName("Should build grounded anti-hallucination context referencing catalog items")
    void testContextGroundingBuildsCatalogFacts() {
        ProductDto dto = productService.mapToDto(product1);

        String context = groundingService.buildGroundedContext(List.of(dto));

        assertNotNull(context);
        assertTrue(context.contains("VERIFIED LIVE CATALOG DATA"));
        assertTrue(context.contains("Noise Cancelling Wireless Headphones"));
        assertTrue(context.contains("$129.99"));
        assertTrue(context.contains("Anti-Hallucination Guardrail"));
    }

    private Product createProduct(Long id, String name, String brand, ProductCategory cat, double price, double rating, String desc) {
        Product p = new Product();
        p.setId(id);
        p.setSku("SKU-" + id);
        p.setName(name);
        p.setBrand(brand);
        p.setCategory(cat);
        p.setPrice(BigDecimal.valueOf(price));
        p.setRating(rating);
        p.setDescription(desc);
        p.setIsAvailable(true);
        return p;
    }

    @Test
    @DisplayName("Should evaluate RAG pipeline achieving >= 90% retrieval relevance target")
    void testRagEvaluationPipelineRelevanceTarget() {
        ProductCategory c1 = new ProductCategory("Electronics", "electronics", "desc", "cpu"); c1.setId(1L);
        ProductCategory c2 = new ProductCategory("Apparel", "apparel", "desc", "shirt"); c2.setId(2L);
        ProductCategory c3 = new ProductCategory("Home", "home", "desc", "home"); c3.setId(3L);
        ProductCategory c4 = new ProductCategory("Wearables", "wearables", "desc", "watch"); c4.setId(4L);

        Product p1 = createProduct(1001L, "AuraSound Wireless Noise Cancelling Headphones", "AuraSound", c1, 149.99, 4.8, "Premium wireless audio headphones.");
        Product p2 = createProduct(1002L, "NovaTech Ergonomic Mechanical Keyboard RGB", "NovaTech", c1, 119.99, 4.5, "Customizable mechanical keyboard.");
        Product p3 = createProduct(1003L, "ApexStyles Breathable Athletic Running Shoes", "ApexStyles", c2, 89.99, 4.7, "Lightweight running shoes.");
        Product p4 = createProduct(1004L, "ApexStyles Thermal Weatherproof Winter Jacket Parka", "ApexStyles", c2, 179.99, 4.6, "Heavy duty parka jacket.");
        Product p5 = createProduct(1005L, "OmniHome Stainless Drip Coffee Maker Brewer", "OmniHome", c3, 99.99, 4.4, "Fresh morning drip coffee maker.");
        Product p6 = createProduct(1006L, "OmniHome Autonomous Robotic Smart Floor Vacuum Cleaner", "OmniHome", c3, 279.99, 4.7, "Smart vacuum floor cleaner.");
        Product p7 = createProduct(1007L, "ChronosTech Fitness Tracking Smartwatch AMOLED", "ChronosTech", c4, 199.99, 4.8, "Modern fitness smartwatch.");
        Product p8 = createProduct(1008L, "VentureGear Water-Resistant Commuter Laptop Backpack", "VentureGear", c4, 69.99, 4.6, "Spacious commuter backpack.");

        List.of(p1, p2, p3, p4, p5, p6, p7, p8).forEach(p -> {
            productRepository.save(p);
            vectorStoreService.indexProduct(p);
        });

        var eval = evaluationService.evaluatePipeline();

        assertNotNull(eval);
        assertTrue(eval.totalScenariosTested >= 5);
        assertTrue(eval.retrievalRelevancePercent >= 90.0, "Target >= 90% retrieval relevance, actual: " + eval.retrievalRelevancePercent);
        assertTrue(eval.correctnessPercent >= 95.0);
    }
}
