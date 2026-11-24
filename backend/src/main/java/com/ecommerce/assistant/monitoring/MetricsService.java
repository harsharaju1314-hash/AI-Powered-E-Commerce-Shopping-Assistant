package com.ecommerce.assistant.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final MeterRegistry registry;

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordAgentExecution(String agentType, String toolName, String status, long durationMs) {
        Counter.builder("ecommerce.ai.agent.executions")
            .tag("agent", agentType)
            .tag("tool", toolName != null ? toolName : "none")
            .tag("status", status)
            .register(registry)
            .increment();

        Timer.builder("ecommerce.ai.agent.duration")
            .tag("agent", agentType)
            .register(registry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordOrderCreated(String status) {
        Counter.builder("ecommerce.orders.created")
            .tag("status", status)
            .register(registry)
            .increment();
    }

    public void recordCartOperation(String action) {
        Counter.builder("ecommerce.cart.operations")
            .tag("action", action)
            .register(registry)
            .increment();
    }

    public void recordRagQuery(boolean hit) {
        Counter.builder("ecommerce.rag.queries")
            .tag("cache_hit", String.valueOf(hit))
            .register(registry)
            .increment();
    }

    public void recordDatabaseQueryTime(long durationMs) {
        Timer.builder("ecommerce.db.response.time")
            .register(registry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
