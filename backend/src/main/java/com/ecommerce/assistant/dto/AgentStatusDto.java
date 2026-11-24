package com.ecommerce.assistant.dto;

public class AgentStatusDto {
    private String agentName;
    private String state;
    private String currentAction;

    public AgentStatusDto() {}

    public AgentStatusDto(String agentName, String state, String currentAction) {
        this.agentName = agentName;
        this.state = state;
        this.currentAction = currentAction;
    }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCurrentAction() { return currentAction; }
    public void setCurrentAction(String currentAction) { this.currentAction = currentAction; }
}
