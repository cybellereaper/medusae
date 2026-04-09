package com.github.cybellereaper.medusae.servicea.application.workflow;

import java.util.List;

public record WorkflowExecutionResult(
        boolean success,
        List<String> executedNodeIds,
        List<WorkflowValidationError> errors
) {
    public WorkflowExecutionResult {
        executedNodeIds = executedNodeIds == null ? List.of() : List.copyOf(executedNodeIds);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
