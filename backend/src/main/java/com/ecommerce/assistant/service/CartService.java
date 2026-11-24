package com.ecommerce.assistant.service;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.CartItem;
import com.ecommerce.assistant.entity.Inventory;
import com.ecommerce.assistant.entity.Product;
import com.ecommerce.assistant.entity.ShoppingCart;
import com.ecommerce.assistant.entity.User;
import com.ecommerce.assistant.exception.BadRequestException;
import com.ecommerce.assistant.exception.InventoryShortageException;
import com.ecommerce.assistant.exception.ResourceNotFoundException;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final MetricsService metricsService;

    public CartService(
        ShoppingCartRepository cartRepository,
        CartItemRepository itemRepository,
        ProductRepository productRepository,
        InventoryRepository inventoryRepository,
        UserRepository userRepository,
        MetricsService metricsService
    ) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.metricsService = metricsService;
    }

    @Transactional
    public ShoppingCart getOrCreateCartEntity(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            ShoppingCart newCart = new ShoppingCart(user);
            return cartRepository.save(newCart);
        });
    }

    @Transactional(readOnly = true)
    public CartDto getCart(Long userId) {
        ShoppingCart cart = getOrCreateCartEntity(userId);
        return mapToDto(cart);
    }

    @Transactional
    public CartDto addToCart(Long userId, AddToCartRequest req) {
        ShoppingCart cart = getOrCreateCartEntity(userId);
        Product product = productRepository.findById(req.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.getProductId()));

        if (!Boolean.TRUE.equals(product.getIsAvailable())) {
            throw new BadRequestException("Product is currently unavailable: " + product.getName());
        }

        Inventory inv = inventoryRepository.findByProductId(product.getId())
            .orElseThrow(() -> new InventoryShortageException("No inventory record for product: " + product.getName()));

        Optional<CartItem> existingItemOpt = itemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        int newQuantity = req.getQuantity();
        if (existingItemOpt.isPresent()) {
            newQuantity += existingItemOpt.get().getQuantity();
        }

        if (inv.getAvailableQuantity() < newQuantity) {
            throw new InventoryShortageException(
                "Requested quantity (" + newQuantity + ") exceeds available inventory (" + inv.getAvailableQuantity() + ") for " + product.getName()
            );
        }

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setQuantity(newQuantity);
            item.setUnitPrice(product.getPrice());
            itemRepository.save(item);
        } else {
            CartItem item = new CartItem(cart, product, req.getQuantity(), product.getPrice());
            cart.getItems().add(item);
            itemRepository.save(item);
        }

        metricsService.recordCartOperation("add");
        return getCart(userId);
    }

    @Transactional
    public CartDto updateQuantity(Long userId, Long productId, UpdateCartItemRequest req) {
        ShoppingCart cart = getOrCreateCartEntity(userId);

        CartItem item = itemRepository.findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart: product " + productId));

        if (req.getQuantity() <= 0) {
            return removeItem(userId, productId);
        }

        Inventory inv = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new InventoryShortageException("No inventory record for product: " + productId));

        if (inv.getAvailableQuantity() < req.getQuantity()) {
            throw new InventoryShortageException("Requested quantity (" + req.getQuantity() + ") exceeds available inventory (" + inv.getAvailableQuantity() + ")");
        }

        item.setQuantity(req.getQuantity());
        itemRepository.save(item);

        metricsService.recordCartOperation("update");
        return getCart(userId);
    }

    @Transactional
    public CartDto removeItem(Long userId, Long productId) {
        ShoppingCart cart = getOrCreateCartEntity(userId);
        itemRepository.findByCartIdAndProductId(cart.getId(), productId).ifPresent(item -> {
            cart.getItems().remove(item);
            itemRepository.delete(item);
        });

        metricsService.recordCartOperation("remove");
        return getCart(userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        ShoppingCart cart = getOrCreateCartEntity(userId);
        cart.getItems().clear();
        itemRepository.deleteByCartId(cart.getId());
        cart.setDiscountCode(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
        metricsService.recordCartOperation("clear");
    }

    @Transactional
    public CartDto applyDiscount(Long userId, ApplyDiscountRequest req) {
        ShoppingCart cart = getOrCreateCartEntity(userId);
        String code = req.getDiscountCode().toUpperCase().trim();

        BigDecimal discount = BigDecimal.ZERO;
        if ("SAVE10".equals(code)) {
            discount = cart.calculateSubtotal().multiply(BigDecimal.valueOf(0.10));
        } else if ("SAVE20".equals(code)) {
            discount = cart.calculateSubtotal().multiply(BigDecimal.valueOf(0.20));
        } else if ("WELCOME50".equals(code)) {
            discount = BigDecimal.valueOf(50.00);
        } else {
            throw new BadRequestException("Invalid discount coupon code: " + code);
        }

        cart.setDiscountCode(code);
        cart.setDiscountAmount(discount);
        cartRepository.save(cart);

        return mapToDto(cart);
    }

    public CartDto mapToDto(ShoppingCart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setStatus(cart.getStatus());
        dto.setDiscountCode(cart.getDiscountCode());
        dto.setDiscountAmount(cart.getDiscountAmount());
        dto.setSubtotal(cart.calculateSubtotal());
        dto.setTotal(cart.calculateTotal());

        dto.setItems(cart.getItems().stream().map(item -> {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setProductSku(item.getProduct().getSku());
            itemDto.setProductImage(item.getProduct().getImageUrl());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSubtotal(item.getSubtotal());
            return itemDto;
        }).collect(Collectors.toList()));

        return dto;
    }
}
