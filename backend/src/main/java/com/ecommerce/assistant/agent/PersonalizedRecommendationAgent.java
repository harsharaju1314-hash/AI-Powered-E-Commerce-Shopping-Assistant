package com.ecommerce.assistant.agent;

import com.ecommerce.assistant.dto.ChatMessageResponse;
import com.ecommerce.assistant.dto.ProductDto;
import com.ecommerce.assistant.dto.UserProfileResponse;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.tools.AgentTool;
import com.ecommerce.assistant.tools.RecommendationTools;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PersonalizedRecommendationAgent implements SpecializedAgent {

    private final RecommendationTools recommendationTools;
    private final MetricsService metricsService;

    public PersonalizedRecommendationAgent(
        RecommendationTools recommendationTools,
        MetricsService metricsService
    ) {
        this.recommendationTools = recommendationTools;
        this.metricsService = metricsService;
    }

    @Override
    public String getName() {
        return "RECOMMENDATION";
    }

    @Override
    public String getRole() {
        return "Personalized Recommendation Agent: Analyzes customer profile preferences, browsing history, and grounds recommendations in the verified catalog.";
    }

    @Override
    public List<AgentTool> getTools() {
        return recommendationTools.getTools();
    }

    @Override
    public ChatMessageResponse process(String query, Long userId, String sessionId, List<String> previousContext) {
        long start = System.currentTimeMillis();
        List<String> toolsExecuted = new ArrayList<>();
        ChatMessageResponse response = new ChatMessageResponse(sessionId, "", getName());

        // 1. Fetch user preferences if user is authenticated
        UserProfileResponse profile = null;
        if (userId != null) {
            AgentTool prefTool = getTools().stream().filter(t -> t.getName().equals("getUserPreferences")).findFirst().orElseThrow();
            toolsExecuted.add("getUserPreferences");
            Object prefResult = prefTool.execute(Map.of(), userId, sessionId);
            if (prefResult instanceof UserProfileResponse up) {
                profile = up;
            }
        }

        // 2. Retrieve personalized products
        AgentTool recommendTool = getTools().stream().filter(t -> t.getName().equals("recommendProducts")).findFirst().orElseThrow();
        toolsExecuted.add("recommendProducts");
        @SuppressWarnings("unchecked")
        List<ProductDto> recommended = (List<ProductDto>) recommendTool.execute(Map.of("limit", 4), userId, sessionId);

        response.setProducts(recommended);

        StringBuilder sb = new StringBuilder();
        if (profile != null && profile.getFullName() != null) {
            sb.append("Hello ").append(profile.getFullName()).append("! Based on your preferred categories ");
            if (profile.getPreferredCategories() != null && !profile.getPreferredCategories().isEmpty()) {
                sb.append("(").append(String.join(", ", profile.getPreferredCategories())).append(") ");
            }
            if (profile.getMaxBudget() != null) {
                sb.append("and budget limit of $").append(profile.getMaxBudget()).append(", ");
            }
            sb.append("here are our top recommended selections for you:\n\n");
        } else {
            sb.append("Here are our top trending, highest-rated product recommendations for you:\n\n");
        }

        for (int i = 0; i < recommended.size(); i++) {
            ProductDto p = recommended.get(i);
            sb.append(i + 1).append(". **").append(p.getName()).append("** — $").append(p.getPrice())
              .append("\n   *Why this is recommended:* High verified rating of ").append(p.getRating()).append("/5.0 and excellent customer satisfaction.\n");
        }

        response.setReply(sb.toString());
        response.setToolCallsExecuted(toolsExecuted);
        metricsService.recordAgentExecution(getName(), String.join(",", toolsExecuted), "SUCCESS", System.currentTimeMillis() - start);
        return response;
    }
}
