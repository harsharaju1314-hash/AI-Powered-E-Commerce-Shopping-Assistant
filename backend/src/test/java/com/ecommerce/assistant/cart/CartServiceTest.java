package com.ecommerce.assistant.cart;

import com.ecommerce.assistant.FakeRepositories.*;
import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.*;
import com.ecommerce.assistant.exception.BadRequestException;
import com.ecommerce.assistant.exception.InventoryShortageException;
import com.ecommerce.assistant.monitoring.MetricsService;
import com.ecommerce.assistant.service.CartService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {

    private FakeShoppingCartRepository cartRepository;
    private FakeCartItemRepository itemRepository;
    private FakeProductRepository productRepository;
    private FakeInventoryRepository inventoryRepository;
    private FakeUserRepository userRepository;
    private MetricsService metricsService;
    private CartService cartService;

    private User testUser;
    private ShoppingCart cart;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        cartRepository = new FakeShoppingCartRepository();
        itemRepository = new FakeCartItemRepository();
        productRepository = new FakeProductRepository();
        inventoryRepository = new FakeInventoryRepository();
        userRepository = new FakeUserRepository();
        metricsService = new MetricsService(new SimpleMeterRegistry());

        cartService = new CartService(
            cartRepository,
            itemRepository,
            productRepository,
            inventoryRepository,
            userRepository,
            metricsService
        );

        testUser = new User("buyer@example.com", "hash", "Buyer");
        testUser.setId(5L);
        userRepository.save(testUser);

        cart = new ShoppingCart(testUser);
        cart.setId(10L);
        cart.setItems(new ArrayList<>());
        cartRepository.save(cart);

        product = new Product();
        product.setId(201L);
        product.setSku("PROD-201");
        product.setName("Ergonomic Keyboard");
        product.setPrice(BigDecimal.valueOf(99.00));
        product.setIsAvailable(true);
        productRepository.save(product);

        inventory = new Inventory(product, 25);
        inventoryRepository.save(inventory);
    }

    @Test
    @DisplayName("Should add available product to cart and calculate correct subtotal")
    void testAddToCartSuccess() {
        AddToCartRequest req = new AddToCartRequest(201L, 2);

        CartDto result = cartService.addToCart(5L, req);

        assertNotNull(result);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(198.00), result.getSubtotal());
    }

    @Test
    @DisplayName("Should throw InventoryShortageException when adding more than available stock")
    void testAddToCartInsufficientInventory() {
        AddToCartRequest req = new AddToCartRequest(201L, 30);

        assertThrows(InventoryShortageException.class, () -> cartService.addToCart(5L, req));
    }

    @Test
    @DisplayName("Should throw BadRequestException when product is marked unavailable")
    void testAddToCartUnavailableProduct() {
        product.setIsAvailable(false);
        AddToCartRequest req = new AddToCartRequest(201L, 1);

        assertThrows(BadRequestException.class, () -> cartService.addToCart(5L, req));
    }

    @Test
    @DisplayName("Should update quantity of an existing cart item")
    void testUpdateQuantitySuccess() {
        CartItem item = new CartItem(cart, product, 1, product.getPrice());
        cart.getItems().add(item);
        itemRepository.save(item);

        CartDto result = cartService.updateQuantity(5L, 201L, new UpdateCartItemRequest(4));

        assertNotNull(result);
        assertEquals(4, item.getQuantity());
    }

    @Test
    @DisplayName("Should remove item when updated quantity is set to 0")
    void testUpdateQuantityToZeroRemovesItem() {
        CartItem item = new CartItem(cart, product, 2, product.getPrice());
        cart.getItems().add(item);
        itemRepository.save(item);

        CartDto result = cartService.updateQuantity(5L, 201L, new UpdateCartItemRequest(0));

        assertNotNull(result);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should apply valid discount code and recalculate cart total")
    void testApplyDiscountSuccess() {
        CartItem item = new CartItem(cart, product, 2, product.getPrice()); // 2 * $99 = $198
        cart.getItems().add(item);
        itemRepository.save(item);

        CartDto result = cartService.applyDiscount(5L, new ApplyDiscountRequest("SAVE10"));

        assertNotNull(result);
        assertEquals("SAVE10", result.getDiscountCode());
        assertEquals(0, BigDecimal.valueOf(19.80).compareTo(result.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(178.20).compareTo(result.getTotal()));
    }

    @Test
    @DisplayName("Should throw BadRequestException on invalid promo coupon code")
    void testApplyInvalidDiscountThrowsBadRequest() {
        assertThrows(BadRequestException.class, () ->
            cartService.applyDiscount(5L, new ApplyDiscountRequest("INVALID_CODE"))
        );
    }
}
