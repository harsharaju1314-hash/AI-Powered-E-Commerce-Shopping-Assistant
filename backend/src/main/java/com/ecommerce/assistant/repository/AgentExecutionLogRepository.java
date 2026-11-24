package com.ecommerce.assistant.repository;

import com.ecommerce.assistant.entity.AgentExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentExecutionLogRepository extends JpaRepository<AgentExecutionLog, Long> {
    List<AgentExecutionLog> findBySessionIdOrderByCreatedAtAsc(String sessionId);
}
