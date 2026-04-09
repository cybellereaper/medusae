package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowEdge;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowNode;

import java.time.Instant;
import java.util.*;

public final class WorkflowEngine {

    private final WorkflowValidator validator;
    private final WorkflowActionExecutorPort actionExecutor;
    private final WorkflowAuditPort auditPort;

    public WorkflowEngine(WorkflowValidator validator,
                          WorkflowActionExecutorPort actionExecutor,
                          WorkflowAuditPort auditPort) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.actionExecutor = Objects.requireNonNull(actionExecutor, "actionExecutor");
        this.auditPort = Objects.requireNonNull(auditPort, "auditPort");
    }

    public WorkflowExecutionResult execute(WorkflowExecutionRequest request) {
        WorkflowValidationResult validation = validator.validate(request.workflow());
        if (!validation.valid()) {
            return new WorkflowExecutionResult(false, List.of(), validation.errors());
        }

        List<String> orderedNodeIds = resolveDeterministicOrder(request.workflow());
        for (String nodeId : orderedNodeIds) {
            WorkflowNode node = request.workflow().nodes().stream()
                    .filter(candidate -> candidate.id().equals(nodeId))
                    .findFirst()
                    .orElseThrow();

            if (request.mode() == WorkflowExecutionMode.LIVE) {
                actionExecutor.execute(request.workflow(), node);
            }

            auditPort.append(new WorkflowAuditEntry(
                    request.workflow().tenantId(),
                    request.workflow().workflowId(),
                    request.mode() == WorkflowExecutionMode.LIVE ? "workflow.execute.node" : "workflow.simulate.node",
                    request.actorUserId(),
                    request.correlationId(),
                    Instant.now(),
                    Map.of("nodeId", nodeId, "action", node.action().name())
            ));
        }

        return new WorkflowExecutionResult(true, orderedNodeIds, List.of());
    }

    private List<String> resolveDeterministicOrder(WorkflowDefinition definition) {
        Map<String, Set<String>> outgoing = new HashMap<>();
        Map<String, Integer> incomingCount = new HashMap<>();

        for (WorkflowNode node : definition.nodes()) {
            outgoing.put(node.id(), new TreeSet<>());
            incomingCount.put(node.id(), 0);
        }

        for (WorkflowEdge edge : definition.edges()) {
            outgoing.get(edge.fromNodeId()).add(edge.toNodeId());
            incomingCount.put(edge.toNodeId(), incomingCount.get(edge.toNodeId()) + 1);
        }

        PriorityQueue<String> queue = new PriorityQueue<>();
        incomingCount.forEach((nodeId, count) -> {
            if (count == 0) {
                queue.add(nodeId);
            }
        });

        List<String> ordered = new ArrayList<>(definition.nodes().size());
        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            ordered.add(nodeId);

            for (String next : outgoing.get(nodeId)) {
                int updatedCount = incomingCount.get(next) - 1;
                incomingCount.put(next, updatedCount);
                if (updatedCount == 0) {
                    queue.add(next);
                }
            }
        }

        if (ordered.size() != definition.nodes().size()) {
            throw new IllegalStateException("Workflow contains a cycle or disconnected invalid graph");
        }

        return List.copyOf(ordered);
    }
}
