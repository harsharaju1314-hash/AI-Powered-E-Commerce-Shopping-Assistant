package com.ecommerce.assistant.repository;

import com.ecommerce.assistant.entity.Order;
import com.ecommerce.assistant.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStatus(OrderStatus status);
}
