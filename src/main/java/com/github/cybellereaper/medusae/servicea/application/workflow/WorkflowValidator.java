package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowEdge;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowNode;

import java.util.*;

public final class WorkflowValidator {

    public WorkflowValidationResult validate(WorkflowDefinition definition) {
        List<WorkflowValidationError> errors = new ArrayList<>();
        if (definition == null) {
            return new WorkflowValidationResult(List.of(new WorkflowValidationError("workflow", "workflow is required")));
        }

        if (isBlank(definition.tenantId())) {
            errors.add(new WorkflowValidationError("tenantId", "tenantId is required"));
        }
        if (isBlank(definition.workflowId())) {
            errors.add(new WorkflowValidationError("workflowId", "workflowId is required"));
        }
        if (definition.version() <= 0) {
            errors.add(new WorkflowValidationError("version", "version must be > 0"));
        }
        if (definition.triggers().isEmpty()) {
            errors.add(new WorkflowValidationError("triggers", "at least one trigger is required"));
        }
        if (definition.nodes().isEmpty()) {
            errors.add(new WorkflowValidationError("nodes", "at least one action node is required"));
        }

        Set<String> nodeIds = new HashSet<>();
        for (WorkflowNode node : definition.nodes()) {
            if (isBlank(node.id())) {
                errors.add(new WorkflowValidationError("nodes.id", "node id is required"));
                continue;
            }
            if (!nodeIds.add(node.id())) {
                errors.add(new WorkflowValidationError("nodes.id", "duplicate node id: " + node.id()));
            }
        }

        for (WorkflowEdge edge : definition.edges()) {
            if (!nodeIds.contains(edge.fromNodeId())) {
                errors.add(new WorkflowValidationError("edges.fromNodeId", "unknown from node: " + edge.fromNodeId()));
            }
            if (!nodeIds.contains(edge.toNodeId())) {
                errors.add(new WorkflowValidationError("edges.toNodeId", "unknown to node: " + edge.toNodeId()));
            }
        }

        return new WorkflowValidationResult(errors);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
