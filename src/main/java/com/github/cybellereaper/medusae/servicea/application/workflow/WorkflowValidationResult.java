package com.github.cybellereaper.medusae.servicea.application.workflow;

import java.util.List;

public record WorkflowValidationResult(List<WorkflowValidationError> errors) {

    public WorkflowValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public boolean valid() {
        return errors.isEmpty();
    }
}
