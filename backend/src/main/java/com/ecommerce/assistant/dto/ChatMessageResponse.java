package com.ecommerce.assistant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageResponse {
    private String sessionId;
    private String reply;
    private String activeAgent; // PRODUCT_DISCOVERY, RECOMMENDATION, CART_MANAGEMENT, ORDER_TRACKING
    private List<ProductDto> products;
    private ProductComparisonDto comparison;
    private PendingApprovalDto pendingApproval;
    private List<String> toolCallsExecuted = new ArrayList<>();
    private List<String> suggestedActions = new ArrayList<>();
    private Instant timestamp = Instant.now();

    public ChatMessageResponse() {}

    public ChatMessageResponse(String sessionId, String reply, String activeAgent) {
        this.sessionId = sessionId;
        this.reply = reply;
        this.activeAgent = activeAgent;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    @JsonProperty("session_token")
    public String getSessionToken() { return sessionId; }
    public void setSessionToken(String sessionToken) { this.sessionId = sessionToken; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    @JsonProperty("response")
    public String getResponse() { return reply; }
    public void setResponse(String response) { this.reply = response; }

    public String getActiveAgent() { return activeAgent; }
    public void setActiveAgent(String activeAgent) { this.activeAgent = activeAgent; }

    @JsonProperty("active_agent")
    public String getActiveAgentSnake() { return activeAgent; }

    public List<ProductDto> getProducts() { return products; }
    public void setProducts(List<ProductDto> products) { this.products = products; }

    public ProductComparisonDto getComparison() { return comparison; }
    public void setComparison(ProductComparisonDto comparison) { this.comparison = comparison; }

    @JsonProperty("comparison_data")
    public ProductComparisonDto getComparisonData() { return comparison; }

    public PendingApprovalDto getPendingApproval() { return pendingApproval; }
    public void setPendingApproval(PendingApprovalDto pendingApproval) { this.pendingApproval = pendingApproval; }

    @JsonProperty("action_payload")
    public PendingApprovalDto getActionPayload() { return pendingApproval; }

    public List<String> getToolCallsExecuted() { return toolCallsExecuted; }
    public void setToolCallsExecuted(List<String> toolCallsExecuted) { this.toolCallsExecuted = toolCallsExecuted; }

    @JsonProperty("agent_thoughts")
    public List<String> getAgentThoughts() { return toolCallsExecuted; }

    @JsonProperty("suggested_actions")
    public List<String> getSuggestedActions() {
        if (suggestedActions == null || suggestedActions.isEmpty()) {
            return List.of(
                "Find wireless headphones under $100",
                "Compare top rated mechanical keyboards",
                "Recommend running shoes for fitness",
                "View my shopping cart"
            );
        }
        return suggestedActions;
    }
    public void setSuggestedActions(List<String> suggestedActions) { this.suggestedActions = suggestedActions; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
