package com.ecommerce.assistant.repository;

import com.ecommerce.assistant.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
    List<ConversationMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
}
