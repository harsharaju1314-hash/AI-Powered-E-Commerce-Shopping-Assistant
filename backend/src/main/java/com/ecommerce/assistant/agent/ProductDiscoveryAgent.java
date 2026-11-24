package com.ecommerce.assistant.agent;

import com.ecommerce.assistant.dto.ChatMessageResponse;
import com.ecommerce.assistant.dto.ProductComparisonDto;
import com.ecommerce.assistant.dto.ProductDto;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.rag.ContextGroundingService;
import com.ecommerce.assistant.tools.AgentTool;
import com.ecommerce.assistant.tools.ProductDiscoveryTools;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProductDiscoveryAgent implements SpecializedAgent {

    private final ProductDiscoveryTools discoveryTools;
    private final ContextGroundingService groundingService;
    private final MetricsService metricsService;

    public ProductDiscoveryAgent(
        ProductDiscoveryTools discoveryTools,
        ContextGroundingService groundingService,
        MetricsService metricsService
    ) {
        this.discoveryTools = discoveryTools;
        this.groundingService = groundingService;
        this.metricsService = metricsService;
    }

    @Override
    public String getName() {
        return "PRODUCT_DISCOVERY";
    }

    @Override
    public String getRole() {
        return "Product Discovery Agent: Interprets natural language, runs semantic RAG search, and performs side-by-side product comparisons.";
    }

    @Override
    public List<AgentTool> getTools() {
        return discoveryTools.getTools();
    }

    @Override
    public ChatMessageResponse process(String query, Long userId, String sessionId, List<String> previousContext) {
        long start = System.currentTimeMillis();
        List<String> toolsExecuted = new ArrayList<>();
        ChatMessageResponse response = new ChatMessageResponse(sessionId, "", getName());

        // Check if query is asking to compare
        if (query.toLowerCase().contains("compare") || query.toLowerCase().contains("difference between")) {
            AgentTool compareTool = getTools().stream().filter(t -> t.getName().equals("compareProducts")).findFirst().orElseThrow();
            // Extract or search products to compare
            AgentTool searchTool = getTools().stream().filter(t -> t.getName().equals("searchProducts")).findFirst().orElseThrow();
            toolsExecuted.add("searchProducts");
            @SuppressWarnings("unchecked")
            List<ProductDto> found = (List<ProductDto>) searchTool.execute(Map.of("query", query.replace("compare", "").trim()), userId, sessionId);

            if (found.size() >= 2) {
                toolsExecuted.add("compareProducts");
                List<Long> ids = List.of(found.get(0).getId(), found.get(1).getId());
                ProductComparisonDto comparison = (ProductComparisonDto) compareTool.execute(Map.of("productIds", ids), userId, sessionId);
                response.setComparison(comparison);
                response.setProducts(found.subList(0, 2));
                response.setReply("Here is the side-by-side comparison for the products you requested:\n" + comparison.getAiSummary());
            } else {
                response.setProducts(found);
                response.setReply("I found " + found.size() + " matching item(s). Please select at least two items to generate a full comparison.");
            }
        } else {
            // Semantic Search via RAG
            AgentTool searchTool = getTools().stream().filter(t -> t.getName().equals("searchProducts")).findFirst().orElseThrow();
            toolsExecuted.add("searchProducts");

            // Extract potential price constraints: e.g. "under 100", "under $50", "below 200"
            BigDecimal maxPrice = extractMaxPrice(query);
            Map<String, Object> searchArgs = new HashMap<>();
            searchArgs.put("query", query);
            if (maxPrice != null) searchArgs.put("maxPrice", maxPrice);

            @SuppressWarnings("unchecked")
            List<ProductDto> products = (List<ProductDto>) searchTool.execute(searchArgs, userId, sessionId);

            response.setProducts(products);
            if (products.isEmpty()) {
                response.setReply("I couldn't find any catalog items matching your criteria. Try adjusting your price limit or search keywords.");
            } else {
                StringBuilder reply = new StringBuilder();
                reply.append("Found ").append(products.size()).append(" verified products matching your request:\n\n");
                for (int i = 0; i < products.size(); i++) {
                    ProductDto p = products.get(i);
                    reply.append(i + 1).append(". **").append(p.getName()).append("** — $").append(p.getPrice())
                         .append(" (Rating: ").append(p.getRating()).append("/5.0)\n")
                         .append("   *").append(p.getDescription()).append("*\n");
                }
                response.setReply(reply.toString());
            }
        }

        response.setToolCallsExecuted(toolsExecuted);
        metricsService.recordAgentExecution(getName(), String.join(",", toolsExecuted), "SUCCESS", System.currentTimeMillis() - start);
        return response;
    }

    private BigDecimal extractMaxPrice(String query) {
        Pattern pattern = Pattern.compile("(?i)(?:under|below|less than|max)\\s*\\$?\\s*(\\d+(?:\\.\\d{1,2})?)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1));
            } catch (Exception ignored) {}
        }
        return null;
    }
}
