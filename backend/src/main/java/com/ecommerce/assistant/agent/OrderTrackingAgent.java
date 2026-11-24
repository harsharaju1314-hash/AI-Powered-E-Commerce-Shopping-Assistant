package com.ecommerce.assistant.agent;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.ApprovalRequest;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.service.ApprovalService;
import com.ecommerce.assistant.tools.AgentTool;
import com.ecommerce.assistant.tools.OrderTools;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OrderTrackingAgent implements SpecializedAgent {

    private final OrderTools orderTools;
    private final ApprovalService approvalService;
    private final MetricsService metricsService;

    public OrderTrackingAgent(OrderTools orderTools, ApprovalService approvalService, MetricsService metricsService) {
        this.orderTools = orderTools;
        this.approvalService = approvalService;
        this.metricsService = metricsService;
    }

    @Override
    public String getName() {
        return "ORDER_TRACKING";
    }

    @Override
    public String getRole() {
        return "Order Tracking Agent: Checks shipment timeline, order history, and securely initiates checkout or cancellation with approval verification.";
    }

    @Override
    public List<AgentTool> getTools() {
        return orderTools.getTools();
    }

    @Override
    public ChatMessageResponse process(String query, Long userId, String sessionId, List<String> previousContext) {
        long start = System.currentTimeMillis();
        List<String> toolsExecuted = new ArrayList<>();
        ChatMessageResponse response = new ChatMessageResponse(sessionId, "", getName());

        if (userId == null) {
            response.setReply("Please sign in to track your orders or manage checkout.");
            return response;
        }

        String qLower = query.toLowerCase();

        // 1. Checkout / Place order request -> requires human approval checkpoint
        if (qLower.contains("checkout") || qLower.contains("place order") || qLower.contains("buy now")) {
            AgentTool createOrderTool = getTools().stream().filter(t -> t.getName().equals("createOrder")).findFirst().orElseThrow();
            toolsExecuted.add("createOrder");
            Object result = createOrderTool.execute(Map.of("shippingAddress", "123 Innovation Way, Suite 400"), userId, sessionId);

            if (result instanceof ApprovalRequest req) {
                response.setPendingApproval(approvalService.mapToDto(req));
                response.setReply("Sensitive Action Checkpoint: I have prepared your checkout summary. " +
                    "To prevent unintended charges, please click **Approve** or **Reject** to confirm order placement.\n\n" +
                    "**Order Summary:** " + req.getSummary());
            } else {
                response.setReply(result.toString());
            }
        }
        // 2. Cancellation request -> requires human approval checkpoint
        else if (qLower.contains("cancel")) {
            String orderNumber = extractOrderNumber(query);
            if (orderNumber != null) {
                AgentTool cancelTool = getTools().stream().filter(t -> t.getName().equals("requestOrderCancellation")).findFirst().orElseThrow();
                toolsExecuted.add("requestOrderCancellation");
                Object result = cancelTool.execute(Map.of("orderNumber", orderNumber), userId, sessionId);
                if (result instanceof ApprovalRequest req) {
                    response.setPendingApproval(approvalService.mapToDto(req));
                    response.setReply("Sensitive Action Checkpoint: Order cancellation is irreversible. " +
                        "Please review and confirm below.\n\n" +
                        "**Request:** " + req.getSummary());
                } else {
                    response.setReply(result.toString());
                }
            } else {
                response.setReply("Please provide the specific order number (e.g. ORD-12345) you wish to cancel.");
            }
        }
        // 3. Status tracking of a specific order
        else if (extractOrderNumber(query) != null) {
            String orderNumber = extractOrderNumber(query);
            AgentTool statusTool = getTools().stream().filter(t -> t.getName().equals("getOrderStatus")).findFirst().orElseThrow();
            toolsExecuted.add("getOrderStatus");
            try {
                OrderStatusResponse status = (OrderStatusResponse) statusTool.execute(Map.of("orderNumber", orderNumber), userId, sessionId);
                response.setReply("Status for Order **#" + status.getOrderNumber() + "**:\n" +
                    "- **Status:** " + status.getStatus() + "\n" +
                    "- **Estimated Delivery:** " + (status.getEstimatedDelivery() != null ? status.getEstimatedDelivery().toString() : "TBD") + "\n" +
                    "- **Carrier Update:** " + status.getTrackingDescription());
            } catch (Exception e) {
                response.setReply("Could not find order #" + orderNumber + ": " + e.getMessage());
            }
        }
        // 4. Order history overview
        else {
            AgentTool ordersTool = getTools().stream().filter(t -> t.getName().equals("getOrders")).findFirst().orElseThrow();
            toolsExecuted.add("getOrders");
            @SuppressWarnings("unchecked")
            List<OrderDto> orders = (List<OrderDto>) ordersTool.execute(Map.of("page", 0, "size", 5), userId, sessionId);

            if (orders.isEmpty()) {
                response.setReply("You do not have any past orders yet. Once you complete simulated checkout, they will appear here.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Here is your recent order history:\n\n");
                for (OrderDto o : orders) {
                    sb.append("- Order **#").append(o.getOrderNumber()).append("** — $").append(o.getTotalAmount())
                      .append(" [").append(o.getStatus()).append("]\n");
                }
                sb.append("\nAsk me 'track order #...' to get live shipment updates or 'cancel order #...' if needed.");
                response.setReply(sb.toString());
            }
        }

        response.setToolCallsExecuted(toolsExecuted);
        metricsService.recordAgentExecution(getName(), String.join(",", toolsExecuted), "SUCCESS", System.currentTimeMillis() - start);
        return response;
    }

    private String extractOrderNumber(String query) {
        Pattern p = Pattern.compile("(?i)(ORD-\\d+-[A-Z0-9]+)");
        Matcher m = p.matcher(query);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
