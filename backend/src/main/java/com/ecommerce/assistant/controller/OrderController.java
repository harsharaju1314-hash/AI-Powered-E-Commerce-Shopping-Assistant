package com.ecommerce.assistant.controller;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.security.OwnershipValidator;
import com.ecommerce.assistant.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders & Fulfillment", description = "Endpoints for simulated checkout, order placement, tracking, and cancellation")
public class OrderController {

    private final OrderService orderService;
    private final OwnershipValidator ownershipValidator;

    public OrderController(OrderService orderService, OwnershipValidator ownershipValidator) {
        this.orderService = orderService;
        this.ownershipValidator = ownershipValidator;
    }

    @PostMapping({"", "/checkout", "/confirm", "/prepare"})
    @Operation(summary = "Create and checkout order from shopping cart")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(orderService.createOrder(userId, request));
    }

    @GetMapping({"", "/user"})
    @Operation(summary = "Get user's order history with pagination")
    public ResponseEntity<PageResponse<OrderDto>> getUserOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(orderService.getUserOrders(userId, page, size));
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "Get order details by order number")
    public ResponseEntity<OrderDto> getOrderByNumber(@PathVariable String orderNumber) {
        OrderDto order = orderService.getOrderByNumber(orderNumber);
        ownershipValidator.validateOwnership(order.getUserId());
        return ResponseEntity.ok(order);
    }

    @GetMapping({"/{orderNumber}/status", "/track/{orderNumber}"})
    @Operation(summary = "Get live order tracking status and delivery estimates")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderStatus(orderNumber));
    }

    @PostMapping("/{orderNumber}/cancel")
    @Operation(summary = "Cancel an eligible order and restore inventory")
    public ResponseEntity<OrderDto> cancelOrder(
        @PathVariable String orderNumber,
        @RequestBody(required = false) CancelOrderRequest request
    ) {
        Long userId = ownershipValidator.getCurrentUserId();
        CancelOrderRequest req = request != null ? request : new CancelOrderRequest("User requested cancellation");
        return ResponseEntity.ok(orderService.cancelOrder(orderNumber, userId, req));
    }
}
