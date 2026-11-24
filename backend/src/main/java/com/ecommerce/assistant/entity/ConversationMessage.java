package com.ecommerce.assistant.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conversation_messages")
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;

    @Column(name = "sender_type", nullable = false, length = 50)
    private String senderType; // USER, AGENT, SYSTEM

    @Column(name = "agent_type", length = 100)
    private String agentType;

    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ConversationMessage() {}

    public ConversationMessage(ConversationSession session, String senderType, String agentType, String messageContent, String metadataJson) {
        this.session = session;
        this.senderType = senderType;
        this.agentType = agentType;
        this.messageContent = messageContent;
        this.metadataJson = metadataJson;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ConversationSession getSession() { return session; }
    public void setSession(ConversationSession session) { this.session = session; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }

    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
