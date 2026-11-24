package com.ecommerce.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ApprovalDecisionRequest {
    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "(?i)APPROVE|REJECT", message = "Decision must be either APPROVE or REJECT")
    private String decision; // APPROVE or REJECT

    public ApprovalDecisionRequest() {}
    public ApprovalDecisionRequest(String decision) { this.decision = decision; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
}
