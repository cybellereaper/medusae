package com.github.cybellereaper.medusae.servicea.application.workflow;

import java.time.Instant;
import java.util.Map;

public record WorkflowAuditEntry(
        String tenantId,
        String workflowId,
        String eventType,
        String actorUserId,
        String correlationId,
        Instant occurredAt,
        Map<String, String> details
) {
    public WorkflowAuditEntry {
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
