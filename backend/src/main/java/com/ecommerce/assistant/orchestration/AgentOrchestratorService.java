package com.ecommerce.assistant.orchestration;

import com.ecommerce.assistant.agent.*;
import com.ecommerce.assistant.dto.ChatMessageRequest;
import com.ecommerce.assistant.dto.ChatMessageResponse;
import com.ecommerce.assistant.entity.AgentExecutionLog;
import com.ecommerce.assistant.entity.ConversationMessage;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.repository.AgentExecutionLogRepository;
import com.ecommerce.assistant.service.ConversationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class AgentOrchestratorService {

    private final ProductDiscoveryAgent productDiscoveryAgent;
    private final PersonalizedRecommendationAgent recommendationAgent;
    private final CartManagementAgent cartManagementAgent;
    private final OrderTrackingAgent orderTrackingAgent;
    private final ConversationService conversationService;
    private final AgentExecutionLogRepository executionLogRepository;
    private final MetricsService metricsService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final int maxSteps;
    private final int timeoutSeconds;

    public AgentOrchestratorService(
        ProductDiscoveryAgent productDiscoveryAgent,
        PersonalizedRecommendationAgent recommendationAgent,
        CartManagementAgent cartManagementAgent,
        OrderTrackingAgent orderTrackingAgent,
        ConversationService conversationService,
        AgentExecutionLogRepository executionLogRepository,
        MetricsService metricsService,
        RedisTemplate<String, Object> redisTemplate,
        @Value("${app.ai.agent.max-steps:5}") int maxSteps,
        @Value("${app.ai.agent.tool-timeout-seconds:10}") int timeoutSeconds
    ) {
        this.productDiscoveryAgent = productDiscoveryAgent;
        this.recommendationAgent = recommendationAgent;
        this.cartManagementAgent = cartManagementAgent;
        this.orderTrackingAgent = orderTrackingAgent;
        this.conversationService = conversationService;
        this.executionLogRepository = executionLogRepository;
        this.metricsService = metricsService;
        this.redisTemplate = redisTemplate;
        this.maxSteps = maxSteps;
        this.timeoutSeconds = timeoutSeconds;
    }

    public enum AgentIntent {
        PRODUCT_DISCOVERY,
        RECOMMENDATION,
        CART_MANAGEMENT,
        ORDER_TRACKING
    }

    public AgentIntent classifyIntent(String userMessage) {
        String msg = userMessage.toLowerCase().trim();

        // 1. Order tracking / Checkout
        if (msg.contains("order") || msg.contains("track") || msg.contains("delivery") ||
            msg.contains("shipping") || msg.contains("cancel order") || msg.contains("checkout") ||
            msg.startsWith("ord-")) {
            return AgentIntent.ORDER_TRACKING;
        }

        // 2. Cart management
        if (msg.contains("cart") || msg.contains("add to cart") || msg.contains("remove from cart") ||
            msg.contains("quantity") || msg.contains("coupon") || msg.contains("discount code")) {
            return AgentIntent.CART_MANAGEMENT;
        }

        // 3. Personalized recommendations
        if (msg.contains("recommend") || msg.contains("suggest") || msg.contains("for me") ||
            msg.contains("my style") || msg.contains("based on my") || msg.contains("preferences")) {
            return AgentIntent.RECOMMENDATION;
        }

        // 4. Default to Product Discovery (search, compare, filters)
        return AgentIntent.PRODUCT_DISCOVERY;
    }

    public ChatMessageResponse processMessage(ChatMessageRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        String sessionId = request.getSessionId();

        // 1. Ensure conversation session exists
        var session = conversationService.getOrCreateSession(sessionId, userId);
        sessionId = session.getId();

        // 2. Persist incoming user message
        conversationService.saveMessage(sessionId, "USER", "USER", request.getMessage(), null);

        // 3. Intent classification & agent selection
        AgentIntent intent = classifyIntent(request.getMessage());
        SpecializedAgent selectedAgent = switch (intent) {
            case PRODUCT_DISCOVERY -> productDiscoveryAgent;
            case RECOMMENDATION -> recommendationAgent;
            case CART_MANAGEMENT -> cartManagementAgent;
            case ORDER_TRACKING -> orderTrackingAgent;
        };

        // 4. Retrieve recent context (last 5 messages)
        List<String> contextHistory = new ArrayList<>();
        try {
            List<ConversationMessage> recent = conversationService.getSessionMessages(sessionId);
            int start = Math.max(0, recent.size() - 5);
            for (int i = start; i < recent.size(); i++) {
                contextHistory.add(recent.get(i).getSenderType() + ": " + recent.get(i).getMessageContent());
            }
        } catch (Exception ignored) {}

        // 5. Execute agent workflow with bounded retries (3 attempts)
        ChatMessageResponse response = null;
        String executionStatus = "SUCCESS";
        String errorMessage = null;

        int attempts = 0;
        while (attempts < 3 && response == null) {
            attempts++;
            try {
                response = selectedAgent.process(request.getMessage(), userId, sessionId, contextHistory);
            } catch (Exception e) {
                executionStatus = "FAILURE";
                errorMessage = e.getMessage();
            }
        }

        // Graceful fallback if all attempts failed
        if (response == null) {
            response = new ChatMessageResponse(
                sessionId,
                "I apologize, but our AI assistant experienced a momentary delay processing your request. Please try rephrasing your search or checking your connection.",
                selectedAgent.getName()
            );
        }

        long duration = System.currentTimeMillis() - startTime;

        // 6. Record persistent audit log
        AgentExecutionLog log = new AgentExecutionLog(
            sessionId,
            selectedAgent.getName(),
            response.getToolCallsExecuted() != null ? String.join(",", response.getToolCallsExecuted()) : "none",
            "query: " + request.getMessage(),
            "intent: " + intent,
            duration,
            executionStatus,
            errorMessage
        );
        executionLogRepository.save(log);

        // 7. Save agent response to conversation history
        conversationService.saveMessage(sessionId, "AGENT", selectedAgent.getName(), response.getReply(), null);

        // 8. Record metrics
        metricsService.recordAgentExecution(selectedAgent.getName(), log.getToolName(), executionStatus, duration);

        return response;
    }
}
