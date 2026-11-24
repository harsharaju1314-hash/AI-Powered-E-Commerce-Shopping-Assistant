package com.ecommerce.assistant.tools;

import com.ecommerce.assistant.dto.CreateOrderRequest;
import com.ecommerce.assistant.entity.ApprovalRequest;
import com.ecommerce.assistant.service.ApprovalService;
import com.ecommerce.assistant.service.CartService;
import com.ecommerce.assistant.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderTools {

    private final OrderService orderService;
    private final CartService cartService;
    private final ApprovalService approvalService;

    public OrderTools(OrderService orderService, CartService cartService, ApprovalService approvalService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.approvalService = approvalService;
    }

    public List<AgentTool> getTools() {
        return List.of(
            new AgentTool() {
                @Override public String getName() { return "getOrders"; }
                @Override public String getDescription() { return "Retrieve the customer's previous order history with tracking numbers and statuses"; }
                @Override public String getParametersSchema() { return "page (number), size (number)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return List.of();
                    int page = args.get("page") != null ? Integer.parseInt(args.get("page").toString()) : 0;
                    int size = args.get("size") != null ? Integer.parseInt(args.get("size").toString()) : 10;
                    return orderService.getUserOrders(userId, page, size).getContent();
                }
            },
            new AgentTool() {
                @Override public String getName() { return "getOrderStatus"; }
                @Override public String getDescription() { return "Check live fulfillment status and delivery timeline for an order number"; }
                @Override public String getParametersSchema() { return "orderNumber (string)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    String orderNumber = args.get("orderNumber").toString();
                    return orderService.getOrderStatus(orderNumber);
                }
            },
            new AgentTool() {
                @Override public String getName() { return "getDeliveryDetails"; }
                @Override public String getDescription() { return "Retrieve estimated carrier arrival and shipping address for an order"; }
                @Override public String getParametersSchema() { return "orderNumber (string)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    String orderNumber = args.get("orderNumber").toString();
                    return orderService.getOrderByNumber(orderNumber);
                }
            },
            new AgentTool() {
                @Override public String getName() { return "createOrder"; }
                @Override public String getDescription() {
                    return "Initiate checkout and order placement. NOTE: Sensitive action requiring explicit user human approval token.";
                }
                @Override public String getParametersSchema() { return "shippingAddress (string)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return "User must be authenticated to place an order.";
                    var cart = cartService.getCart(userId);
                    if (cart.getItems().isEmpty()) {
                        return "Cannot place order: Cart is currently empty.";
                    }
                    String address = args.getOrDefault("shippingAddress", "Standard User Profile Address").toString();
                    // Intercept and create approval checkpoint!
                    ApprovalRequest req = approvalService.createApprovalRequest(
                        userId,
                        sessionId,
                        "ORDER_PLACEMENT",
                        address,
                        "Place order for " + cart.getItems().size() + " items totaling $" + cart.getTotal() + " to " + address
                    );
                    return req;
                }
            },
            new AgentTool() {
                @Override public String getName() { return "requestOrderCancellation"; }
                @Override public String getDescription() {
                    return "Initiate cancellation workflow for an order. NOTE: Sensitive action requiring human approval token.";
                }
                @Override public String getParametersSchema() { return "orderNumber (string)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return "Authentication required to cancel orders.";
                    String orderNumber = args.get("orderNumber").toString();
                    ApprovalRequest req = approvalService.createApprovalRequest(
                        userId,
                        sessionId,
                        "ORDER_CANCELLATION",
                        orderNumber,
                        "Cancel order #" + orderNumber + " and restock inventory"
                    );
                    return req;
                }
            }
        );
    }
}
