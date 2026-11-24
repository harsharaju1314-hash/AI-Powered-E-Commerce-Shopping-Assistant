package com.ecommerce.assistant.tools;

import com.ecommerce.assistant.dto.ProductDto;
import com.ecommerce.assistant.dto.UserProfileResponse;
import com.ecommerce.assistant.rag.VectorStoreService;
import com.ecommerce.assistant.service.AuthService;
import com.ecommerce.assistant.service.OrderService;
import com.ecommerce.assistant.service.ProductService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class RecommendationTools {

    private final AuthService authService;
    private final OrderService orderService;
    private final ProductService productService;
    private final VectorStoreService vectorStoreService;

    public RecommendationTools(
        AuthService authService,
        OrderService orderService,
        ProductService productService,
        VectorStoreService vectorStoreService
    ) {
        this.authService = authService;
        this.orderService = orderService;
        this.productService = productService;
        this.vectorStoreService = vectorStoreService;
    }

    public List<AgentTool> getTools() {
        return List.of(
            new AgentTool() {
                @Override public String getName() { return "getUserPreferences"; }
                @Override public String getDescription() { return "Retrieve the authorized customer's saved shopping preferences and budget limits"; }
                @Override public String getParametersSchema() { return "none"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return "Guest user with standard default preferences.";
                    return authService.getProfile(userId);
                }
            },
            new AgentTool() {
                @Override public String getName() { return "getPurchaseHistory"; }
                @Override public String getDescription() { return "Retrieve previous orders placed by the current customer for personalization"; }
                @Override public String getParametersSchema() { return "none"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return List.of();
                    return orderService.getUserOrders(userId, 0, 5).getContent();
                }
            },
            new AgentTool() {
                @Override public String getName() { return "retrieveRelevantProducts"; }
                @Override public String getDescription() { return "Retrieve products matching preference vectors and price limits via RAG"; }
                @Override public String getParametersSchema() { return "topic (string), maxBudget (number)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    String topic = (String) args.getOrDefault("topic", "popular");
                    BigDecimal budget = args.get("maxBudget") != null ? new BigDecimal(args.get("maxBudget").toString()) : null;
                    return vectorStoreService.similaritySearch(topic, null, budget, 4.0, 4);
                }
            },
            new AgentTool() {
                @Override public String getName() { return "recommendProducts"; }
                @Override public String getDescription() { return "Get curated, top-rated products tailored to customer profile"; }
                @Override public String getParametersSchema() { return "limit (number)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    int limit = args.get("limit") != null ? Integer.parseInt(args.get("limit").toString()) : 4;
                    return productService.getRecommendedProducts(userId, limit);
                }
            }
        );
    }
}
