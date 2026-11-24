package com.ecommerce.assistant.agent;

import com.ecommerce.assistant.FakeRepositories.*;
import com.ecommerce.assistant.dto.ChatMessageRequest;
import com.ecommerce.assistant.dto.ChatMessageResponse;
import com.ecommerce.assistant.entity.AgentExecutionLog;
import com.ecommerce.assistant.entity.ConversationSession;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.orchestration.AgentOrchestratorService;
import com.ecommerce.assistant.service.ConversationService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AgentOrchestratorServiceTest {

    private AgentOrchestratorService orchestrator;
    private FakeAgentExecutionLogRepository logRepository;
    private FakeConversationSessionRepository sessionRepository;
    private FakeConversationMessageRepository messageRepository;
    private FakeUserRepository userRepository;
    private ConversationService conversationService;
    private MetricsService metricsService;

    private AtomicBoolean discoveryCalled;
    private AtomicBoolean recommendationCalled;
    private AtomicBoolean cartCalled;
    private AtomicBoolean orderCalled;

    @BeforeEach
    void setUp() {
        discoveryCalled = new AtomicBoolean(false);
        recommendationCalled = new AtomicBoolean(false);
        cartCalled = new AtomicBoolean(false);
        orderCalled = new AtomicBoolean(false);

        logRepository = new FakeAgentExecutionLogRepository();
        sessionRepository = new FakeConversationSessionRepository();
        messageRepository = new FakeConversationMessageRepository();
        userRepository = new FakeUserRepository();
        conversationService = new ConversationService(sessionRepository, messageRepository, userRepository);
        metricsService = new MetricsService(new SimpleMeterRegistry());

        ProductDiscoveryAgent discoveryAgent = new ProductDiscoveryAgent(null, null, metricsService) {
            @Override
            public ChatMessageResponse process(String query, Long userId, String sessionId, List<String> previousContext) {
                discoveryCalled.set(true);
                ChatMessageResponse res = new ChatMessageResponse(sessionId, "Here are matched products", "PRODUCT_DISCOVERY");
                res.setToolCallsExecuted(List.of("searchProducts"));
                return res;
            }
        };

        PersonalizedRecommendationAgent recommendationAgent = new PersonalizedRecommendationAgent(null, metricsService) {
            @Override
            public ChatMessageResponse process(String query, Long userId, String sessionId, List<String> previousContext) {
                recommendationCalled.set(true);
                ChatMessageResponse res = new ChatMessageResponse(sessionId, "Here are recommended items", "RECOMMENDATION");
                res.setToolCallsExecuted(List.of("recommendProducts"));
                return res;
            }
        };

        CartManagementAgent cartAgent = new CartManagementAgent(null, null, metricsService) {
            @Override
            public ChatMessageResponse process(String query, Long userId, String sessionId, List<String> previousContext) {
                cartCalled.set(true);
                ChatMessageResponse res = new ChatMessageResponse(sessionId, "Cart updated successfully", "CART_MANAGEMENT");
                res.setToolCallsExecuted(List.of("addToCart"));
                return res;
            }
        };

        OrderTrackingAgent orderAgent = new OrderTrackingAgent(null, null, metricsService) {
            @Override
            public ChatMessageResponse process(String query, Long userId, String sessionId, List<String> previousContext) {
                orderCalled.set(true);
                ChatMessageResponse res = new ChatMessageResponse(sessionId, "Order status is SHIPPED", "ORDER_TRACKING");
                res.setToolCallsExecuted(List.of("getOrderStatus"));
                return res;
            }
        };

        orchestrator = new AgentOrchestratorService(
            discoveryAgent,
            recommendationAgent,
            cartAgent,
            orderAgent,
            conversationService,
            logRepository,
            metricsService,
            null, // redisTemplate optional fallback
            5,
            10
        );
    }

    @Test
    @DisplayName("Should classify intent accurately based on message keywords")
    void testIntentClassification() {
        assertEquals(AgentOrchestratorService.AgentIntent.ORDER_TRACKING, orchestrator.classifyIntent("Where is my order?"));
        assertEquals(AgentOrchestratorService.AgentIntent.ORDER_TRACKING, orchestrator.classifyIntent("Please track order ORD-9921"));
        assertEquals(AgentOrchestratorService.AgentIntent.ORDER_TRACKING, orchestrator.classifyIntent("I want to proceed to checkout"));

        assertEquals(AgentOrchestratorService.AgentIntent.CART_MANAGEMENT, orchestrator.classifyIntent("Add item 101 to cart"));
        assertEquals(AgentOrchestratorService.AgentIntent.CART_MANAGEMENT, orchestrator.classifyIntent("Show my current cart items"));
        assertEquals(AgentOrchestratorService.AgentIntent.CART_MANAGEMENT, orchestrator.classifyIntent("Apply discount coupon SAVE10"));

        assertEquals(AgentOrchestratorService.AgentIntent.RECOMMENDATION, orchestrator.classifyIntent("What do you recommend for me?"));
        assertEquals(AgentOrchestratorService.AgentIntent.RECOMMENDATION, orchestrator.classifyIntent("Suggest top gear based on my preferences"));

        assertEquals(AgentOrchestratorService.AgentIntent.PRODUCT_DISCOVERY, orchestrator.classifyIntent("Find noise cancelling headphones under $100"));
        assertEquals(AgentOrchestratorService.AgentIntent.PRODUCT_DISCOVERY, orchestrator.classifyIntent("Compare item 101 and 102"));
    }

    @Test
    @DisplayName("Should route discovery queries to ProductDiscoveryAgent and persist execution log")
    void testProcessMessageRoutesToDiscovery() {
        ChatMessageRequest req = new ChatMessageRequest("session-101", "Find wireless headphones");
        ChatMessageResponse response = orchestrator.processMessage(req, 1L);

        assertNotNull(response);
        assertEquals("PRODUCT_DISCOVERY", response.getActiveAgent());
        assertTrue(discoveryCalled.get());
        assertEquals("Here are matched products", response.getReply());

        // Verify log persistence
        List<AgentExecutionLog> logs = logRepository.findBySessionIdOrderByCreatedAtAsc("session-101");
        assertFalse(logs.isEmpty());
        assertEquals("PRODUCT_DISCOVERY", logs.get(0).getAgentType());
        assertEquals("SUCCESS", logs.get(0).getStatus());
    }

    @Test
    @DisplayName("Should route cart operations to CartManagementAgent")
    void testProcessMessageRoutesToCart() {
        ChatMessageRequest req = new ChatMessageRequest("session-102", "Add item 101 to cart");
        ChatMessageResponse response = orchestrator.processMessage(req, 1L);

        assertNotNull(response);
        assertEquals("CART_MANAGEMENT", response.getActiveAgent());
        assertTrue(cartCalled.get());
        assertEquals("Cart updated successfully", response.getReply());
    }

    @Test
    @DisplayName("Should route recommendation queries to PersonalizedRecommendationAgent")
    void testProcessMessageRoutesToRecommendation() {
        ChatMessageRequest req = new ChatMessageRequest("session-103", "What products do you recommend for me?");
        ChatMessageResponse response = orchestrator.processMessage(req, 1L);

        assertNotNull(response);
        assertEquals("RECOMMENDATION", response.getActiveAgent());
        assertTrue(recommendationCalled.get());
        assertEquals("Here are recommended items", response.getReply());
    }

    @Test
    @DisplayName("Should route tracking and checkout queries to OrderTrackingAgent")
    void testProcessMessageRoutesToOrder() {
        ChatMessageRequest req = new ChatMessageRequest("session-104", "Where is my order ORD-2024?");
        ChatMessageResponse response = orchestrator.processMessage(req, 1L);

        assertNotNull(response);
        assertEquals("ORDER_TRACKING", response.getActiveAgent());
        assertTrue(orderCalled.get());
        assertEquals("Order status is SHIPPED", response.getReply());
    }

    @Test
    @DisplayName("Should handle empty queries gracefully with fallback")
    void testEmptyQueryFallback() {
        ChatMessageRequest req = new ChatMessageRequest("session-105", "");
        ChatMessageResponse response = orchestrator.processMessage(req, 1L);

        assertNotNull(response);
        assertTrue(response.getReply().toLowerCase().contains("help") || response.getReply().toLowerCase().contains("welcome") || response.getReply().length() > 0);
    }
}
