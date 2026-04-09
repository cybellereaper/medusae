package com.github.cybellereaper.medusae.servicea.domain.workflow;

import java.util.Map;

public record WorkflowTrigger(
        String id,
        TriggerType type,
        Map<String, String> config
) {
    public WorkflowTrigger {
        config = config == null ? Map.of() : Map.copyOf(config);
    }
}
