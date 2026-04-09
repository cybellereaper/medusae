package com.github.cybellereaper.medusae.servicea.domain.workflow;

import java.util.List;

public record WorkflowDefinition(
        String tenantId,
        String workflowId,
        int version,
        List<WorkflowTrigger> triggers,
        List<WorkflowNode> nodes,
        List<WorkflowEdge> edges
) {
    public WorkflowDefinition {
        triggers = triggers == null ? List.of() : List.copyOf(triggers);
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
    }
}
