package com.ecommerce.assistant.tools;

import com.ecommerce.assistant.dto.AddToCartRequest;
import com.ecommerce.assistant.dto.ApplyDiscountRequest;
import com.ecommerce.assistant.dto.UpdateCartItemRequest;
import com.ecommerce.assistant.service.CartService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CartTools {

    private final CartService cartService;

    public CartTools(CartService cartService) {
        this.cartService = cartService;
    }

    public List<AgentTool> getTools() {
        return List.of(
            new AgentTool() {
                @Override public String getName() { return "getCart"; }
                @Override public String getDescription() { return "Retrieve the current active shopping cart items, subtotal, and discounts"; }
                @Override public String getParametersSchema() { return "none"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return "User must be authenticated to access shopping cart.";
                    return cartService.getCart(userId);
                }
            },
            new AgentTool() {
                @Override public String getName() { return "addToCart"; }
                @Override public String getDescription() { return "Add a verified product to the shopping cart with quantity"; }
                @Override public String getParametersSchema() { return "productId (number), quantity (number)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return "Authentication required to modify shopping cart.";
                    Long productId = Long.valueOf(args.get("productId").toString());
                    int qty = args.get("quantity") != null ? Integer.parseInt(args.get("quantity").toString()) : 1;
                    return cartService.addToCart(userId, new AddToCartRequest(productId, qty));
                }
            },
            new AgentTool() {
                @Override public String getName() { return "removeFromCart"; }
                @Override public String getDescription() { return "Remove an item from the shopping cart by product ID"; }
                @Override public String getParametersSchema() { return "productId (number)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return "Authentication required.";
                    Long productId = Long.valueOf(args.get("productId").toString());
                    return cartService.removeItem(userId, productId);
                }
            },
            new AgentTool() {
                @Override public String getName() { return "updateCartQuantity"; }
                @Override public String getDescription() { return "Update the quantity of a specific item in the cart"; }
                @Override public String getParametersSchema() { return "productId (number), quantity (number)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return "Authentication required.";
                    Long productId = Long.valueOf(args.get("productId").toString());
                    int qty = Integer.parseInt(args.get("quantity").toString());
                    return cartService.updateQuantity(userId, productId, new UpdateCartItemRequest(qty));
                }
            },
            new AgentTool() {
                @Override public String getName() { return "calculateCartTotal"; }
                @Override public String getDescription() { return "Calculate cart subtotal, applicable taxes, shipping fee, and grand total"; }
                @Override public String getParametersSchema() { return "discountCode (optional string)"; }
                @Override public Object execute(Map<String, Object> args, Long userId, String sessionId) {
                    if (userId == null) return "Authentication required.";
                    if (args.get("discountCode") != null) {
                        cartService.applyDiscount(userId, new ApplyDiscountRequest(args.get("discountCode").toString()));
                    }
                    return cartService.getCart(userId);
                }
            }
        );
    }
}
