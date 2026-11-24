package com.ecommerce.assistant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class ChatMessageRequest {
    private String sessionId;

    @NotBlank(message = "Message content cannot be blank")
    private String message;

    private Object confirmationPayload;

    public ChatMessageRequest() {}

    public ChatMessageRequest(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    @JsonProperty("session_token")
    public String getSessionToken() { return sessionId; }
    public void setSessionToken(String sessionToken) { this.sessionId = sessionToken; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @JsonProperty("confirmation_payload")
    public Object getConfirmationPayload() { return confirmationPayload; }
    public void setConfirmationPayload(Object confirmationPayload) { this.confirmationPayload = confirmationPayload; }
}
