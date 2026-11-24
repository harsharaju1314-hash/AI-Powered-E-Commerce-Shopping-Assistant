package com.ecommerce.assistant.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {

    @Id
    @Column(length = 100)
    private String id; // UUID token

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType; // CHECKOUT, ORDER_PLACEMENT, ORDER_CANCELLATION, BULK_CART_REMOVAL

    @Column(name = "action_payload", nullable = false, columnDefinition = "TEXT")
    private String actionPayload;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ApprovalRequest() {}

    public ApprovalRequest(String id, User user, String sessionId, String actionType, String actionPayload, String summary, Instant expiresAt) {
        this.id = id;
        this.user = user;
        this.sessionId = sessionId;
        this.actionType = actionType;
        this.actionPayload = actionPayload;
        this.summary = summary;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt) && status == ApprovalStatus.PENDING;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionPayload() { return actionPayload; }
    public void setActionPayload(String actionPayload) { this.actionPayload = actionPayload; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
