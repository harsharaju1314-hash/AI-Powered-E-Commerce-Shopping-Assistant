package com.ecommerce.assistant.tools;

import com.ecommerce.assistant.dto.ProductComparisonDto;
import com.ecommerce.assistant.dto.ProductDto;
import com.ecommerce.assistant.dto.ProductSearchRequest;
import com.ecommerce.assistant.rag.VectorStoreService;
import com.ecommerce.assistant.service.ProductService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ProductDiscoveryTools {

    private final ProductService productService;
    private final VectorStoreService vectorStoreService;

    public ProductDiscoveryTools(ProductService productService, VectorStoreService vectorStoreService) {
        this.productService = productService;
        this.vectorStoreService = vectorStoreService;
    }

    public List<AgentTool> getTools() {
        return List.of(
            new AgentTool() {
                @Override public String getName() { return "searchProducts"; }
                @Override public String getDescription() { return "Search catalog using semantic RAG vector similarity and filters"; }
                @Override public String getParametersSchema() { return "query (string), categoryId (number), maxPrice (number), minRating (number)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    String query = (String) args.getOrDefault("query", "");
                    Long categoryId = args.get("categoryId") != null ? Long.valueOf(args.get("categoryId").toString()) : null;
                    BigDecimal maxPrice = args.get("maxPrice") != null ? new BigDecimal(args.get("maxPrice").toString()) : null;
                    Double minRating = args.get("minRating") != null ? Double.valueOf(args.get("minRating").toString()) : null;
                    return vectorStoreService.similaritySearch(query, categoryId, maxPrice, minRating, 5);
                }
            },
            new AgentTool() {
                @Override public String getName() { return "getProductDetails"; }
                @Override public String getDescription() { return "Get detailed specifications and verified inventory for a specific product ID"; }
                @Override public String getParametersSchema() { return "productId (number)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    Long productId = Long.valueOf(args.get("productId").toString());
                    return productService.getProductById(productId);
                }
            },
            new AgentTool() {
                @Override public String getName() { return "filterProducts"; }
                @Override public String getDescription() { return "Filter products by price range, brand, category, and minimum rating"; }
                @Override public String getParametersSchema() { return "brand (string), minPrice (number), maxPrice (number), minRating (number), categoryId (number)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    ProductSearchRequest req = new ProductSearchRequest();
                    if (args.get("brand") != null) req.setBrand(args.get("brand").toString());
                    if (args.get("categoryId") != null) req.setCategoryId(Long.valueOf(args.get("categoryId").toString()));
                    if (args.get("minPrice") != null) req.setMinPrice(new BigDecimal(args.get("minPrice").toString()));
                    if (args.get("maxPrice") != null) req.setMaxPrice(new BigDecimal(args.get("maxPrice").toString()));
                    if (args.get("minRating") != null) req.setMinRating(Double.valueOf(args.get("minRating").toString()));
                    req.setSize(5);
                    return productService.searchProducts(req).getContent();
                }
            },
            new AgentTool() {
                @Override public String getName() { return "compareProducts"; }
                @Override public String getDescription() { return "Generate a side-by-side feature comparison between multiple product IDs"; }
                @Override public String getParametersSchema() { return "productIds (list of numbers)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    List<Long> ids = new ArrayList<>();
                    Object raw = args.get("productIds");
                    if (raw instanceof List<?> list) {
                        for (Object o : list) ids.add(Long.valueOf(o.toString()));
                    }
                    return productService.compareProducts(ids);
                }
            }
        );
    }
}
