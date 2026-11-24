package com.ecommerce.assistant.service;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.*;
import com.ecommerce.assistant.exception.ApprovalExpiredException;
import com.ecommerce.assistant.exception.BadRequestException;
import com.ecommerce.assistant.exception.ResourceNotFoundException;
import com.ecommerce.assistant.exception.UnauthorizedException;
import com.ecommerce.assistant.repository.ApprovalRequestRepository;
import com.ecommerce.assistant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    private final ApprovalRequestRepository approvalRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final CartService cartService;
    private final long timeoutMinutes;

    public ApprovalService(
        ApprovalRequestRepository approvalRepository,
        UserRepository userRepository,
        OrderService orderService,
        CartService cartService,
        @Value("${app.ai.agent.approval-timeout-minutes:15}") long timeoutMinutes
    ) {
        this.approvalRepository = approvalRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.cartService = cartService;
        this.timeoutMinutes = timeoutMinutes;
    }

    @Transactional
    public ApprovalRequest createApprovalRequest(Long userId, String sessionId, String actionType, String actionPayload, String summary) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(timeoutMinutes, ChronoUnit.MINUTES);

        ApprovalRequest request = new ApprovalRequest(token, user, sessionId, actionType, actionPayload, summary, expiresAt);
        return approvalRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<PendingApprovalDto> getPendingApprovals(Long userId) {
        return approvalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, ApprovalStatus.PENDING)
            .stream()
            .filter(req -> !req.isExpired())
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public String processDecision(String approvalId, Long userId, ApprovalDecisionRequest decisionReq) {
        ApprovalRequest request = approvalRepository.findByIdAndUserId(approvalId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Approval request not found for user: " + approvalId));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new BadRequestException("Approval request is already " + request.getStatus());
        }

        if (request.isExpired()) {
            request.setStatus(ApprovalStatus.EXPIRED);
            approvalRepository.save(request);
            throw new ApprovalExpiredException("Approval request has expired");
        }

        request.setDecidedAt(Instant.now());

        if ("REJECT".equalsIgnoreCase(decisionReq.getDecision())) {
            request.setStatus(ApprovalStatus.REJECTED);
            approvalRepository.save(request);
            return "Action '" + request.getActionType() + "' was successfully rejected.";
        }

        // Execute authorized sensitive action
        request.setStatus(ApprovalStatus.APPROVED);

        try {
            String executionResult = executeAction(request, userId);
            request.setStatus(ApprovalStatus.EXECUTED);
            approvalRepository.save(request);
            return executionResult;
        } catch (Exception e) {
            request.setStatus(ApprovalStatus.REJECTED);
            approvalRepository.save(request);
            throw new BadRequestException("Action execution failed: " + e.getMessage());
        }
    }

    private String executeAction(ApprovalRequest request, Long userId) {
        switch (request.getActionType()) {
            case "CHECKOUT":
            case "ORDER_PLACEMENT": {
                CreateOrderRequest req = new CreateOrderRequest(
                    "Standard Registered Shipping Address",
                    "APPROV-" + request.getId()
                );
                OrderDto order = orderService.createOrder(userId, req);
                return "Order #" + order.getOrderNumber() + " successfully placed for total $" + order.getTotalAmount() + "!";
            }
            case "ORDER_CANCELLATION": {
                String orderNumber = request.getActionPayload().trim();
                orderService.cancelOrder(orderNumber, userId, new CancelOrderRequest("User requested cancellation via AI assistant"));
                return "Order #" + orderNumber + " has been successfully cancelled.";
            }
            case "BULK_CART_REMOVAL": {
                cartService.clearCart(userId);
                return "Shopping cart has been completely cleared.";
            }
            default:
                throw new BadRequestException("Unknown action type: " + request.getActionType());
        }
    }

    public PendingApprovalDto mapToDto(ApprovalRequest req) {
        return new PendingApprovalDto(
            req.getId(),
            req.getActionType(),
            req.getSummary(),
            req.getActionPayload(),
            req.getStatus().name(),
            req.getExpiresAt()
        );
    }
}
