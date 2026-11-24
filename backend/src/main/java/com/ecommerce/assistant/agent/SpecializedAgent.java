package com.ecommerce.assistant.agent;

import com.ecommerce.assistant.dto.ChatMessageResponse;
import com.ecommerce.assistant.tools.AgentTool;

import java.util.List;

public interface SpecializedAgent {
    String getName();
    String getRole();
    List<AgentTool> getTools();
    ChatMessageResponse process(String query, Long userId, String sessionId, List<String> previousContext);
}
