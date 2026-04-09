package com.github.cybellereaper.medusae.servicea.domain.workflow;

import java.util.Map;

public record WorkflowNode(
        String id,
        ActionType action,
        Map<String, String> config
) {
    public WorkflowNode {
        config = config == null ? Map.of() : Map.copyOf(config);
    }
}
