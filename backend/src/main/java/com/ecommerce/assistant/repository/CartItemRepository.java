package com.ecommerce.assistant.repository;

import com.ecommerce.assistant.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    List<CartItem> findByCartId(Long cartId);
    void deleteByCartId(Long cartId);
}
