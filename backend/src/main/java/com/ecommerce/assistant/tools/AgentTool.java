package com.ecommerce.assistant.tools;

import java.util.Map;

public interface AgentTool {
    String getName();
    String getDescription();
    String getParametersSchema();
    Object execute(Map<String, Object> arguments, Long userId, String sessionId);
}
