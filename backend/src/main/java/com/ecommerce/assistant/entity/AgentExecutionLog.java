package com.ecommerce.assistant.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agent_execution_logs")
public class AgentExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "agent_type", nullable = false, length = 100)
    private String agentType;

    @Column(name = "tool_name", length = 100)
    private String toolName;

    @Column(name = "tool_arguments", columnDefinition = "TEXT")
    private String toolArguments;

    @Column(name = "tool_result", columnDefinition = "TEXT")
    private String toolResult;

    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    @Column(nullable = false, length = 50)
    private String status; // SUCCESS, FAILURE, TIMEOUT

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AgentExecutionLog() {}

    public AgentExecutionLog(String sessionId, String agentType, String toolName, String toolArguments, String toolResult, Long durationMs, String status, String errorMessage) {
        this.sessionId = sessionId;
        this.agentType = agentType;
        this.toolName = toolName;
        this.toolArguments = toolArguments;
        this.toolResult = toolResult;
        this.executionDurationMs = durationMs;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getToolArguments() { return toolArguments; }
    public void setToolArguments(String toolArguments) { this.toolArguments = toolArguments; }

    public String getToolResult() { return toolResult; }
    public void setToolResult(String toolResult) { this.toolResult = toolResult; }

    public Long getExecutionDurationMs() { return executionDurationMs; }
    public void setExecutionDurationMs(Long executionDurationMs) { this.executionDurationMs = executionDurationMs; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
