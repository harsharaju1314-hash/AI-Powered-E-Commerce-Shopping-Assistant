package com.ecommerce.assistant.service;

import com.ecommerce.assistant.dto.ConversationSessionDto;
import com.ecommerce.assistant.entity.ConversationMessage;
import com.ecommerce.assistant.entity.ConversationSession;
import com.ecommerce.assistant.entity.User;
import com.ecommerce.assistant.exception.ResourceNotFoundException;
import com.ecommerce.assistant.repository.ConversationMessageRepository;
import com.ecommerce.assistant.repository.ConversationSessionRepository;
import com.ecommerce.assistant.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationSessionRepository sessionRepository;
    private final ConversationMessageRepository messageRepository;
    private final UserRepository userRepository;

    public ConversationService(
        ConversationSessionRepository sessionRepository,
        ConversationMessageRepository messageRepository,
        UserRepository userRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ConversationSession getOrCreateSession(String sessionId, Long userId) {
        if (sessionId != null && !sessionId.isBlank()) {
            var existing = sessionRepository.findById(sessionId);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        String newId = (sessionId != null && !sessionId.isBlank()) ? sessionId : UUID.randomUUID().toString();
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        ConversationSession newSession = new ConversationSession(newId, user, "Shopping Session " + newId.substring(0, 8));
        return sessionRepository.save(newSession);
    }

    @Transactional
    public ConversationMessage saveMessage(String sessionId, String senderType, String agentType, String content, String metadata) {
        ConversationSession session = sessionRepository.findById(sessionId)
            .orElseGet(() -> sessionRepository.save(new ConversationSession(sessionId, null, "Shopping Session")));

        session.setUpdatedAt(Instant.now());
        sessionRepository.save(session);

        ConversationMessage msg = new ConversationMessage(session, senderType, agentType, content, metadata);
        return messageRepository.save(msg);
    }

    @Transactional(readOnly = true)
    public List<ConversationMessage> getSessionMessages(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional(readOnly = true)
    public List<ConversationSessionDto> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
            .map(s -> new ConversationSessionDto(s.getId(), s.getTitle(), s.getCreatedAt(), s.getUpdatedAt()))
            .collect(Collectors.toList());
    }
}
