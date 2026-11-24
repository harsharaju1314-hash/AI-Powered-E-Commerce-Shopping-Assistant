package com.ecommerce.assistant.dto;

import java.time.Instant;

public class PendingApprovalDto {
    private String id;
    private String actionType;
    private String summary;
    private String actionPayload;
    private String status;
    private Instant expiresAt;

    public PendingApprovalDto() {}

    public PendingApprovalDto(String id, String actionType, String summary, String actionPayload, String status, Instant expiresAt) {
        this.id = id;
        this.actionType = actionType;
        this.summary = summary;
        this.actionPayload = actionPayload;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getActionPayload() { return actionPayload; }
    public void setActionPayload(String actionPayload) { this.actionPayload = actionPayload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
