package com.ecommerce.assistant.agent;

import com.ecommerce.assistant.dto.CartDto;
import com.ecommerce.assistant.dto.ChatMessageResponse;
import com.ecommerce.assistant.dto.ProductDto;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.service.ProductService;
import com.ecommerce.assistant.tools.AgentTool;
import com.ecommerce.assistant.tools.CartTools;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CartManagementAgent implements SpecializedAgent {

    private final CartTools cartTools;
    private final ProductService productService;
    private final MetricsService metricsService;

    public CartManagementAgent(CartTools cartTools, ProductService productService, MetricsService metricsService) {
        this.cartTools = cartTools;
        this.productService = productService;
        this.metricsService = metricsService;
    }

    @Override
    public String getName() {
        return "CART_MANAGEMENT";
    }

    @Override
    public String getRole() {
        return "Cart Management Agent: Manages user shopping cart, modifies item quantities, checks live inventory, and applies discounts.";
    }

    @Override
    public List<AgentTool> getTools() {
        return cartTools.getTools();
    }

    @Override
    public ChatMessageResponse process(String query, Long userId, String sessionId, List<String> previousContext) {
        long start = System.currentTimeMillis();
        List<String> toolsExecuted = new ArrayList<>();
        ChatMessageResponse response = new ChatMessageResponse(sessionId, "", getName());

        if (userId == null) {
            response.setReply("Please log in to your account to view or update your shopping cart.");
            return response;
        }

        String qLower = query.toLowerCase();

        // 1. Add to cart command
        if (qLower.contains("add") || qLower.contains("buy")) {
            Long productId = extractProductId(query);
            int qty = extractQuantity(query);

            if (productId == null) {
                // If ID is not explicitly passed, search for first matching product name
                var searchResult = productService.searchProducts(new com.ecommerce.assistant.dto.ProductSearchRequest() {{
                    setQuery(query.replace("add", "").replace("to cart", "").replace("buy", "").trim());
                    setSize(1);
                }});
                if (!searchResult.getContent().isEmpty()) {
                    productId = searchResult.getContent().get(0).getId();
                }
            }

            if (productId != null) {
                AgentTool addTool = getTools().stream().filter(t -> t.getName().equals("addToCart")).findFirst().orElseThrow();
                toolsExecuted.add("addToCart");
                try {
                    CartDto cart = (CartDto) addTool.execute(Map.of("productId", productId, "quantity", qty), userId, sessionId);
                    ProductDto p = productService.getProductById(productId);
                    response.setReply("Successfully added " + qty + "x **" + p.getName() + "** to your shopping cart! Current cart total: **$" + cart.getTotal() + "** (" + cart.getItems().size() + " items).");
                } catch (Exception e) {
                    response.setReply("Could not add item to cart: " + e.getMessage());
                }
            } else {
                response.setReply("I couldn't identify which product you want to add. Please specify the product name or ID.");
            }
        }
        // 2. Remove from cart
        else if (qLower.contains("remove") || qLower.contains("delete")) {
            Long productId = extractProductId(query);
            if (productId != null) {
                AgentTool removeTool = getTools().stream().filter(t -> t.getName().equals("removeFromCart")).findFirst().orElseThrow();
                toolsExecuted.add("removeFromCart");
                CartDto cart = (CartDto) removeTool.execute(Map.of("productId", productId), userId, sessionId);
                response.setReply("Item removed from your cart. Updated cart total: **$" + cart.getTotal() + "**.");
            } else {
                response.setReply("Please specify the ID of the product you would like to remove from your cart.");
            }
        }
        // 3. Discount code
        else if (qLower.contains("discount") || qLower.contains("coupon") || qLower.contains("promo")) {
            String code = extractPromoCode(query);
            AgentTool totalTool = getTools().stream().filter(t -> t.getName().equals("calculateCartTotal")).findFirst().orElseThrow();
            toolsExecuted.add("calculateCartTotal");
            try {
                CartDto cart = (CartDto) totalTool.execute(Map.of("discountCode", code), userId, sessionId);
                response.setReply("Promo code **" + code + "** applied successfully! You saved $" + cart.getDiscountAmount() + ". New total: **$" + cart.getTotal() + "**.");
            } catch (Exception e) {
                response.setReply("Could not apply discount code: " + e.getMessage());
            }
        }
        // 4. View cart
        else {
            AgentTool getCartTool = getTools().stream().filter(t -> t.getName().equals("getCart")).findFirst().orElseThrow();
            toolsExecuted.add("getCart");
            CartDto cart = (CartDto) getCartTool.execute(Map.of(), userId, sessionId);

            if (cart.getItems().isEmpty()) {
                response.setReply("Your shopping cart is currently empty. Discover our catalog to find exciting products!");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Here is your current shopping cart:\n\n");
                for (var item : cart.getItems()) {
                    sb.append("- **").append(item.getProductName()).append("** x").append(item.getQuantity())
                      .append(" — $").append(item.getSubtotal()).append("\n");
                }
                sb.append("\n**Subtotal:** $").append(cart.getSubtotal());
                if (cart.getDiscountAmount() != null && cart.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    sb.append(" (Discount: -$").append(cart.getDiscountAmount()).append(")");
                }
                sb.append("\n**Total:** $").append(cart.getTotal());
                response.setReply(sb.toString());
            }
        }

        response.setToolCallsExecuted(toolsExecuted);
        metricsService.recordAgentExecution(getName(), String.join(",", toolsExecuted), "SUCCESS", System.currentTimeMillis() - start);
        return response;
    }

    private Long extractProductId(String query) {
        Pattern p = Pattern.compile("(?i)(?:id|product|item|#)\\s*(\\d+)");
        Matcher m = p.matcher(query);
        if (m.find()) {
            return Long.valueOf(m.group(1));
        }
        return null;
    }

    private int extractQuantity(String query) {
        Pattern p = Pattern.compile("(?i)(?:quantity|qty|count|amount|(\\d+)\\s*(?:x|units?|items?))");
        Matcher m = p.matcher(query);
        if (m.find() && m.group(1) != null) {
            return Integer.parseInt(m.group(1));
        }
        return 1;
    }

    private String extractPromoCode(String query) {
        Pattern p = Pattern.compile("(?i)(?:code|promo|coupon)\\s*([A-Za-z0-9]+)");
        Matcher m = p.matcher(query);
        if (m.find()) {
            return m.group(1);
        }
        return "SAVE10";
    }
}
