package com.ecommerce.assistant.controller;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.ConversationMessage;
import com.ecommerce.assistant.orchestration.AgentOrchestratorService;
import com.ecommerce.assistant.security.OwnershipValidator;
import com.ecommerce.assistant.service.ApprovalService;
import com.ecommerce.assistant.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/ai", "/api"})
@Tag(name = "AI Shopping Assistant", description = "Endpoints for conversational multi-agent interaction, session history, and human approval checkpoints")
public class AiAssistantController {

    private final AgentOrchestratorService orchestratorService;
    private final ConversationService conversationService;
    private final ApprovalService approvalService;
    private final OwnershipValidator ownershipValidator;

    public AiAssistantController(
        AgentOrchestratorService orchestratorService,
        ConversationService conversationService,
        ApprovalService approvalService,
        OwnershipValidator ownershipValidator
    ) {
        this.orchestratorService = orchestratorService;
        this.conversationService = conversationService;
        this.approvalService = approvalService;
        this.ownershipValidator = ownershipValidator;
    }

    @PostMapping({"/chat", "/ai/chat"})
    @Operation(summary = "Submit a natural-language message to the AI Assistant")
    public ResponseEntity<ChatMessageResponse> chat(@Valid @RequestBody ChatMessageRequest request) {
        Long userId = null;
        try {
            userId = ownershipValidator.getCurrentUserId();
        } catch (Exception ignored) {
            // Unauthenticated guest user
        }
        return ResponseEntity.ok(orchestratorService.processMessage(request, userId));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get all conversation sessions for the current user")
    public ResponseEntity<List<ConversationSessionDto>> getSessions() {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(conversationService.getUserSessions(userId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get all messages for a specific session")
    public ResponseEntity<List<ConversationMessage>> getSessionMessages(@PathVariable String sessionId) {
        return ResponseEntity.ok(conversationService.getSessionMessages(sessionId));
    }

    @GetMapping("/approvals/pending")
    @Operation(summary = "Get active pending approval requests for high-impact shopping actions")
    public ResponseEntity<List<PendingApprovalDto>> getPendingApprovals() {
        Long userId = ownershipValidator.getCurrentUserId();
        return ResponseEntity.ok(approvalService.getPendingApprovals(userId));
    }

    @PostMapping("/approvals/{id}/action")
    @Operation(summary = "Explicitly approve or reject a sensitive shopping action")
    public ResponseEntity<Map<String, String>> decideApproval(
        @PathVariable String id,
        @Valid @RequestBody ApprovalDecisionRequest decisionRequest
    ) {
        Long userId = ownershipValidator.getCurrentUserId();
        String result = approvalService.processDecision(id, userId, decisionRequest);
        return ResponseEntity.ok(Map.of("message", result, "decision", decisionRequest.getDecision()));
    }
}
