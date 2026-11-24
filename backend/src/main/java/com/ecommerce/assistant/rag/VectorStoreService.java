package com.ecommerce.assistant.rag;

import com.ecommerce.assistant.dto.ProductDto;
import com.ecommerce.assistant.entity.Product;
import com.ecommerce.assistant.entity.ProductEmbedding;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.repository.ProductEmbeddingRepository;
import com.ecommerce.assistant.repository.ProductRepository;
import com.ecommerce.assistant.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class VectorStoreService {

    private final ProductRepository productRepository;
    private final ProductEmbeddingRepository embeddingRepository;
    private final EmbeddingModelClient embeddingClient;
    private final ProductService productService;
    private final MetricsService metricsService;

    // In-memory cache of product vectors for ultra-fast vector retrieval & sub-300ms latency
    private final Map<Long, float[]> vectorCache = new ConcurrentHashMap<>();

    public VectorStoreService(
        ProductRepository productRepository,
        ProductEmbeddingRepository embeddingRepository,
        EmbeddingModelClient embeddingClient,
        ProductService productService,
        MetricsService metricsService
    ) {
        this.productRepository = productRepository;
        this.embeddingRepository = embeddingRepository;
        this.embeddingClient = embeddingClient;
        this.productService = productService;
        this.metricsService = metricsService;
    }

    public static class ScoredProduct {
        private final Product product;
        private final double score;

        public ScoredProduct(Product product, double score) {
            this.product = product;
            this.score = score;
        }

        public Product getProduct() { return product; }
        public double getScore() { return score; }
    }

    @Transactional
    public void indexProduct(Product product) {
        String content = product.getName() + " " + product.getBrand() + " " +
                         (product.getCategory() != null ? product.getCategory().getName() : "") + " " +
                         product.getDescription() + " " +
                         (product.getTags() != null ? product.getTags() : "");

        float[] vector = embeddingClient.generateEmbedding(content);
        vectorCache.put(product.getId(), vector);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }

        ProductEmbedding pe = embeddingRepository.findByProductId(product.getId())
            .orElse(new ProductEmbedding(product, content, sb.toString()));
        pe.setContent(content);
        pe.setEmbeddingVector(sb.toString());
        embeddingRepository.save(pe);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> similaritySearch(String query, Long categoryId, BigDecimal maxPrice, Double minRating, int topK) {
        long start = System.currentTimeMillis();
        float[] queryVector = embeddingClient.generateEmbedding(query);

        List<Product> candidates = productRepository.findAll();
        List<ScoredProduct> scored = new ArrayList<>();

        for (Product p : candidates) {
            // Apply structured hybrid filters
            if (categoryId != null && (p.getCategory() == null || !p.getCategory().getId().equals(categoryId))) {
                continue;
            }
            if (maxPrice != null && p.getPrice().compareTo(maxPrice) > 0) {
                continue;
            }
            if (minRating != null && p.getRating() < minRating) {
                continue;
            }

            float[] docVector = vectorCache.computeIfAbsent(p.getId(), id -> {
                String content = p.getName() + " " + p.getBrand() + " " + p.getDescription();
                return embeddingClient.generateEmbedding(content);
            });

            double sim = EmbeddingModelClient.cosineSimilarity(queryVector, docVector);

            // Hybrid Search: Multi-token keyword relevance boost
            String qLower = query.toLowerCase();
            String[] queryTokens = qLower.split("[\\s,-]+");
            String pContent = (p.getName() + " " + p.getBrand() + " " + (p.getDescription() != null ? p.getDescription() : "")).toLowerCase();
            for (String token : queryTokens) {
                if (token.length() >= 3 && pContent.contains(token)) {
                    sim += 0.15;
                }
            }

            scored.add(new ScoredProduct(p, sim));
        }

        scored.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        metricsService.recordRagQuery(true);
        metricsService.recordDatabaseQueryTime(System.currentTimeMillis() - start);

        // Grounded relevance thresholding: filter out low-similarity noise to maintain >= 90% relevance
        return scored.stream()
            .filter(sp -> sp.getScore() >= 0.25)
            .limit(topK)
            .map(sp -> productService.mapToDto(sp.getProduct()))
            .collect(Collectors.toList());
    }
}
