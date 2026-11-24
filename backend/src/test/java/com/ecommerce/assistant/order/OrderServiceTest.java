package com.ecommerce.assistant.order;

import com.ecommerce.assistant.FakeRepositories.*;
import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.*;
import com.ecommerce.assistant.exception.BadRequestException;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.service.CartService;
import com.ecommerce.assistant.service.OrderService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private FakeOrderRepository orderRepository;
    private FakeOrderItemRepository orderItemRepository;
    private FakeShoppingCartRepository cartRepository;
    private FakeCartItemRepository cartItemRepository;
    private FakeInventoryRepository inventoryRepository;
    private FakeUserRepository userRepository;
    private FakeProductRepository productRepository;
    private CartService cartService;
    private MetricsService metricsService;
    private OrderService orderService;

    private User testUser;
    private ShoppingCart cart;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        orderRepository = new FakeOrderRepository();
        orderItemRepository = new FakeOrderItemRepository();
        cartRepository = new FakeShoppingCartRepository();
        cartItemRepository = new FakeCartItemRepository();
        inventoryRepository = new FakeInventoryRepository();
        userRepository = new FakeUserRepository();
        productRepository = new FakeProductRepository();
        metricsService = new MetricsService(new SimpleMeterRegistry());

        cartService = new CartService(
            cartRepository,
            cartItemRepository,
            productRepository,
            inventoryRepository,
            userRepository,
            metricsService
        );

        orderService = new OrderService(
            orderRepository,
            orderItemRepository,
            cartRepository,
            inventoryRepository,
            userRepository,
            cartService,
            metricsService
        );

        testUser = new User("shopper@example.com", "hash", "Shopper");
        testUser.setId(3L);
        userRepository.save(testUser);

        product = new Product();
        product.setId(301L);
        product.setName("Smartwatch AMOLED");
        product.setPrice(BigDecimal.valueOf(199.99));
        product.setIsAvailable(true);
        productRepository.save(product);

        inventory = new Inventory(product, 15);
        inventoryRepository.save(inventory);

        cart = new ShoppingCart(testUser);
        cart.setId(8L);
        cart.setItems(new ArrayList<>(List.of(new CartItem(cart, product, 2, product.getPrice()))));
        cartRepository.save(cart);
    }

    @Test
    @DisplayName("Should successfully create order, deduct stock, and clear cart")
    void testCreateOrderSuccess() {
        CreateOrderRequest request = new CreateOrderRequest("456 Tech Park, CA", "IDEMP-001");

        OrderDto created = orderService.createOrder(3L, request);

        assertNotNull(created);
        assertEquals(OrderStatus.CONFIRMED.name(), created.getStatus());
        assertEquals(13, inventory.getAvailableQuantity()); // 15 - 2 = 13
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should return existing order when duplicate idempotency key is submitted")
    void testCreateOrderDuplicateIdempotencyKey() {
        CreateOrderRequest req1 = new CreateOrderRequest("Address", "IDEMP-DUPLICATE");
        OrderDto first = orderService.createOrder(3L, req1);

        // Put another item in cart
        cart.getItems().add(new CartItem(cart, product, 1, product.getPrice()));

        // Submit same key
        OrderDto second = orderService.createOrder(3L, req1);

        assertEquals(first.getOrderNumber(), second.getOrderNumber());
    }

    @Test
    @DisplayName("Should throw BadRequestException when attempting to checkout an empty cart")
    void testCreateOrderEmptyCart() {
        cart.getItems().clear();

        assertThrows(BadRequestException.class, () ->
            orderService.createOrder(3L, new CreateOrderRequest("Address", "KEY-1"))
        );
    }

    @Test
    @DisplayName("Should cancel eligible order and restore inventory")
    void testCancelOrderSuccess() {
        OrderDto created = orderService.createOrder(3L, new CreateOrderRequest("Address", "KEY-CANCEL"));
        assertEquals(13, inventory.getAvailableQuantity());

        OrderDto cancelled = orderService.cancelOrder(created.getOrderNumber(), 3L, new CancelOrderRequest("Customer request"));

        assertEquals(OrderStatus.CANCELLED.name(), cancelled.getStatus());
        assertEquals(15, inventory.getAvailableQuantity()); // 13 + 2 = 15 restored
    }

    @Test
    @DisplayName("Should throw BadRequestException when attempting to cancel shipped order")
    void testCancelShippedOrderThrowsBadRequest() {
        OrderDto created = orderService.createOrder(3L, new CreateOrderRequest("Address", "KEY-SHIPPED"));
        Order order = orderRepository.findByOrderNumber(created.getOrderNumber()).orElseThrow();
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        assertThrows(BadRequestException.class, () ->
            orderService.cancelOrder(created.getOrderNumber(), 3L, new CancelOrderRequest("Cancel"))
        );
    }
}
