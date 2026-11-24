package com.ecommerce.assistant.controller;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.security.OwnershipValidator;
import com.ecommerce.assistant.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Shopping Cart", description = "Endpoints for managing authenticated shopping carts, inventory checks, and discount codes")
public class CartController {

    private final CartService cartService;
    private final OwnershipValidator ownershipValidator;

    public CartController(CartService cartService, OwnershipValidator ownershipValidator) {
        this.cartService = cartService;
        this.ownershipValidator = ownershipValidator;
    }

    @GetMapping
    @Operation(summary = "Get user's shopping cart")
    public ResponseEntity<CartDto> getCart() {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    @Operation(summary = "Add product to shopping cart")
    public ResponseEntity<CartDto> addItem(@Valid @RequestBody AddToCartRequest request) {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(cartService.addToCart(userId, request));
    }

    @RequestMapping(value = "/items/{productId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Update quantity of a cart item")
    public ResponseEntity<CartDto> updateQuantity(
        @PathVariable Long productId,
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(cartService.updateQuantity(userId, productId, request));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartDto> removeItem(@PathVariable Long productId) {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @PostMapping("/discount")
    @Operation(summary = "Apply discount coupon code to cart")
    public ResponseEntity<CartDto> applyDiscount(@Valid @RequestBody ApplyDiscountRequest request) {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(cartService.applyDiscount(userId, request));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear all items from shopping cart")
    public ResponseEntity<Void> clearCart() {
        Long userId = ownershipValidator.getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
