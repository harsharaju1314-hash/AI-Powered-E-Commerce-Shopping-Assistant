package com.ecommerce.assistant.service;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.*;
import com.ecommerce.assistant.exception.BadRequestException;
import com.ecommerce.assistant.exception.ConflictException;
import com.ecommerce.assistant.exception.InventoryShortageException;
import com.ecommerce.assistant.exception.ResourceNotFoundException;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartRepository cartRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final MetricsService metricsService;

    public OrderService(
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository,
        ShoppingCartRepository cartRepository,
        InventoryRepository inventoryRepository,
        UserRepository userRepository,
        CartService cartService,
        MetricsService metricsService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.metricsService = metricsService;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OrderDto createOrder(Long userId, CreateOrderRequest request) {
        // 1. Idempotency check to prevent duplicate order placement
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            var existing = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return mapToDto(existing.get());
            }
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        ShoppingCart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Shopping cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot create order from an empty cart");
        }

        // 2. Validate inventory & deduct stock with locking
        for (CartItem item : cart.getItems()) {
            Inventory inv = inventoryRepository.findByProductIdWithPessimisticLock(item.getProduct().getId())
                .orElseThrow(() -> new InventoryShortageException("No inventory found for: " + item.getProduct().getName()));

            if (inv.getAvailableQuantity() < item.getQuantity()) {
                throw new InventoryShortageException(
                    "Insufficient stock for " + item.getProduct().getName() +
                    ". Available: " + inv.getAvailableQuantity() + ", requested: " + item.getQuantity()
                );
            }

            inv.setAvailableQuantity(inv.getAvailableQuantity() - item.getQuantity());
            inv.setReservedQuantity(inv.getReservedQuantity() + item.getQuantity());
            inventoryRepository.save(inv);
        }

        // 3. Calculate monetary amounts using BigDecimal
        BigDecimal subtotal = cart.calculateSubtotal();
        BigDecimal discount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.08)); // 8% sales tax
        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(100.00)) >= 0 ? BigDecimal.ZERO : BigDecimal.valueOf(9.99); // Free shipping over $100
        BigDecimal total = subtotal.subtract(discount).add(tax).add(shipping);

        // 4. Construct Order entity
        Order order = new Order();
        order.setOrderNumber("ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setTaxAmount(tax);
        order.setShippingAmount(shipping);
        order.setTotalAmount(total);
        order.setShippingAddress(request.getShippingAddress());
        order.setIdempotencyKey(request.getIdempotencyKey());
        order.setEstimatedDelivery(Instant.now().plus(4, ChronoUnit.DAYS));

        Order savedOrder = orderRepository.save(order);

        // 5. Create Order Items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                savedOrder,
                cartItem.getProduct(),
                cartItem.getProduct().getName(),
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                cartItem.getSubtotal()
            );
            orderItemRepository.save(orderItem);
            savedOrder.getItems().add(orderItem);
        }

        // 6. Clear shopping cart upon successful order placement
        cartService.clearCart(userId);

        metricsService.recordOrderCreated(savedOrder.getStatus().name());
        return mapToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderDto> getUserOrders(Long userId, int page, int size) {
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        List<OrderDto> dtos = orders.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
        return new PageResponse<>(dtos, orders.getNumber(), orders.getSize(), orders.getTotalElements(), orders.getTotalPages(), orders.isLast());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        return mapToDto(order);
    }

    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

        String trackingDesc = switch (order.getStatus()) {
            case PENDING -> "Order is awaiting confirmation and inventory reservation.";
            case CONFIRMED -> "Order confirmed! Being prepped at fulfillment center.";
            case PROCESSING -> "Items packaged and passed quality inspection.";
            case SHIPPED -> "En route with express carrier. Delivery on schedule.";
            case DELIVERED -> "Package successfully delivered to destination.";
            case CANCELLED -> "Order has been cancelled. Inventory returned.";
        };

        return new OrderStatusResponse(order.getOrderNumber(), order.getStatus().name(), order.getEstimatedDelivery(), trackingDesc);
    }

    @Transactional
    public OrderDto cancelOrder(String orderNumber, Long userId, CancelOrderRequest request) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized to cancel another user's order");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel order once it has shipped or delivered. Please initiate a return instead.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled.");
        }

        // Restore inventory
        for (OrderItem item : order.getItems()) {
            inventoryRepository.findByProductIdWithPessimisticLock(item.getProduct().getId())
                .ifPresent(inv -> {
                    inv.setAvailableQuantity(inv.getAvailableQuantity() + item.getQuantity());
                    inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - item.getQuantity()));
                    inventoryRepository.save(inv);
                });
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);
        metricsService.recordOrderCreated("CANCELLED");
        return mapToDto(updated);
    }

    public OrderDto mapToDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.setId(o.getId());
        dto.setOrderNumber(o.getOrderNumber());
        dto.setUserId(o.getUser() != null ? o.getUser().getId() : null);
        dto.setStatus(o.getStatus().name());
        dto.setSubtotal(o.getSubtotal());
        dto.setDiscountAmount(o.getDiscountAmount());
        dto.setTaxAmount(o.getTaxAmount());
        dto.setShippingAmount(o.getShippingAmount());
        dto.setTotalAmount(o.getTotalAmount());
        dto.setShippingAddress(o.getShippingAddress());
        dto.setEstimatedDelivery(o.getEstimatedDelivery());
        dto.setCreatedAt(o.getCreatedAt());

        dto.setItems(o.getItems().stream().map(i -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(i.getId());
            itemDto.setProductId(i.getProduct() != null ? i.getProduct().getId() : null);
            itemDto.setProductName(i.getProductName());
            itemDto.setQuantity(i.getQuantity());
            itemDto.setUnitPrice(i.getUnitPrice());
            itemDto.setTotalPrice(i.getTotalPrice());
            return itemDto;
        }).collect(Collectors.toList()));

        return dto;
    }
}
