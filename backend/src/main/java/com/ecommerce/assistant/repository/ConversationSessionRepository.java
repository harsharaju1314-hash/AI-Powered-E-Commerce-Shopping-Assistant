package com.ecommerce.assistant.repository;

import com.ecommerce.assistant.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, String> {
    List<ConversationSession> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
