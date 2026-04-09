package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;

public record WorkflowExecutionRequest(
        WorkflowDefinition workflow,
        WorkflowExecutionMode mode,
        String actorUserId,
        String correlationId
) {
}
