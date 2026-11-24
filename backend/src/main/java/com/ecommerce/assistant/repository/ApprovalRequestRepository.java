package com.ecommerce.assistant.repository;

import com.ecommerce.assistant.entity.ApprovalRequest;
import com.ecommerce.assistant.entity.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, String> {
    List<ApprovalRequest> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ApprovalStatus status);
    Optional<ApprovalRequest> findByIdAndUserId(String id, Long userId);
}
