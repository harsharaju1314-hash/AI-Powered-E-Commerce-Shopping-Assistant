package com.ecommerce.assistant.approval;

import com.ecommerce.assistant.FakeRepositories.*;
import com.ecommerce.assistant.dto.ApprovalDecisionRequest;
import com.ecommerce.assistant.dto.CreateOrderRequest;
import com.ecommerce.assistant.dto.OrderDto;
import com.ecommerce.assistant.entity.*;
import com.ecommerce.assistant.exception.ApprovalExpiredException;
import com.ecommerce.assistant.exception.BadRequestException;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.service.ApprovalService;
import com.ecommerce.assistant.service.CartService;
import com.ecommerce.assistant.service.OrderService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalServiceTest {

    private FakeApprovalRequestRepository approvalRepository;
    private FakeUserRepository userRepository;
    private FakeProductRepository productRepository;
    private FakeInventoryRepository inventoryRepository;
    private FakeShoppingCartRepository cartRepository;
    private FakeCartItemRepository cartItemRepository;
    private FakeOrderRepository orderRepository;
    private FakeOrderItemRepository orderItemRepository;
    private CartService cartService;
    private OrderService orderService;
    private ApprovalService approvalService;

    private User testUser;
    private Product product;

    @BeforeEach
    void setUp() {
        approvalRepository = new FakeApprovalRequestRepository();
        userRepository = new FakeUserRepository();
        productRepository = new FakeProductRepository();
        inventoryRepository = new FakeInventoryRepository();
        cartRepository = new FakeShoppingCartRepository();
        cartItemRepository = new FakeCartItemRepository();
        orderRepository = new FakeOrderRepository();
        orderItemRepository = new FakeOrderItemRepository();
        MetricsService metrics = new MetricsService(new SimpleMeterRegistry());

        cartService = new CartService(
            cartRepository, cartItemRepository, productRepository, inventoryRepository, userRepository, metrics
        );
        orderService = new OrderService(
            orderRepository, orderItemRepository, cartRepository, inventoryRepository, userRepository, cartService, metrics
        );
        approvalService = new ApprovalService(
            approvalRepository, userRepository, orderService, cartService, 15
        );

        testUser = new User("buyer@example.com", "hash", "Buyer");
        testUser.setId(12L);
        userRepository.save(testUser);

        product = new Product();
        product.setId(101L);
        product.setName("Noise Cancelling Headphones");
        product.setPrice(BigDecimal.valueOf(150.00));
        product.setIsAvailable(true);
        productRepository.save(product);

        inventoryRepository.save(new Inventory(product, 20));

        ShoppingCart cart = new ShoppingCart(testUser);
        cart.setId(1L);
        cart.setItems(new ArrayList<>(List.of(new CartItem(cart, product, 1, product.getPrice()))));
        cartRepository.save(cart);
    }

    @Test
    @DisplayName("Should create approval request with PENDING status and expiration")
    void testCreateApprovalRequest() {
        ApprovalRequest created = approvalService.createApprovalRequest(
            12L, "session-abc", "CHECKOUT", "Address", "Summary"
        );

        assertNotNull(created);
        assertEquals(ApprovalStatus.PENDING, created.getStatus());
        assertTrue(created.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    @DisplayName("Should execute sensitive order action when user approves")
    void testProcessDecisionApprove() {
        ApprovalRequest req = approvalService.createApprovalRequest(
            12L, "session-abc", "CHECKOUT", "Address", "Place order"
        );

        String message = approvalService.processDecision(req.getId(), 12L, new ApprovalDecisionRequest("APPROVE"));

        assertNotNull(message);
        assertTrue(message.contains("Order #"));
        assertEquals(ApprovalStatus.EXECUTED, req.getStatus());
    }

    @Test
    @DisplayName("Should reject sensitive action when user clicks Reject")
    void testProcessDecisionReject() {
        ApprovalRequest req = approvalService.createApprovalRequest(
            12L, "session-abc", "CHECKOUT", "Address", "Place order"
        );

        String message = approvalService.processDecision(req.getId(), 12L, new ApprovalDecisionRequest("REJECT"));

        assertNotNull(message);
        assertTrue(message.contains("rejected"));
        assertEquals(ApprovalStatus.REJECTED, req.getStatus());
    }

    @Test
    @DisplayName("Should throw ApprovalExpiredException when approval token is past expiration")
    void testProcessDecisionExpiredThrowsException() {
        ApprovalRequest req = approvalService.createApprovalRequest(
            12L, "session-abc", "CHECKOUT", "Address", "Place order"
        );
        req.setExpiresAt(Instant.now().minus(5, ChronoUnit.MINUTES));
        approvalRepository.save(req);

        assertThrows(ApprovalExpiredException.class, () ->
            approvalService.processDecision(req.getId(), 12L, new ApprovalDecisionRequest("APPROVE"))
        );
        assertEquals(ApprovalStatus.EXPIRED, req.getStatus());
    }

    @Test
    @DisplayName("Should throw BadRequestException if approval request is not in PENDING state")
    void testProcessDecisionAlreadyDecided() {
        ApprovalRequest req = approvalService.createApprovalRequest(
            12L, "session-abc", "CHECKOUT", "Address", "Place order"
        );
        req.setStatus(ApprovalStatus.EXECUTED);
        approvalRepository.save(req);

        assertThrows(BadRequestException.class, () ->
            approvalService.processDecision(req.getId(), 12L, new ApprovalDecisionRequest("APPROVE"))
        );
    }
}
