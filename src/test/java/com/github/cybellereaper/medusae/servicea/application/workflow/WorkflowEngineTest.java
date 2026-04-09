package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.*;
import com.github.cybellereaper.medusae.servicea.infrastructure.workflow.InMemoryWorkflowAuditPort;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowEngineTest {

    @Test
    void executesInDeterministicOrderInDryRunWithoutCallingExecutor() {
        List<String> executedInLive = new ArrayList<>();
        WorkflowActionExecutorPort executor = (definition, node) -> executedInLive.add(node.id());
        InMemoryWorkflowAuditPort auditPort = new InMemoryWorkflowAuditPort();
        WorkflowEngine engine = new WorkflowEngine(new WorkflowValidator(), executor, auditPort);

        WorkflowDefinition definition = new WorkflowDefinition(
                "guild-1",
                "wf-1",
                1,
                List.of(new WorkflowTrigger("t1", TriggerType.WEBHOOK, Map.of("path", "/ingest"))),
                List.of(
                        new WorkflowNode("a", ActionType.SEND_MESSAGE, Map.of()),
                        new WorkflowNode("b", ActionType.CALL_WEBHOOK, Map.of()),
                        new WorkflowNode("c", ActionType.CREATE_TICKET, Map.of())
                ),
                List.of(
                        new WorkflowEdge("a", "b"),
                        new WorkflowEdge("b", "c")
                )
        );

        WorkflowExecutionResult result = engine.execute(new WorkflowExecutionRequest(
                definition,
                WorkflowExecutionMode.DRY_RUN,
                "user-1",
                "corr-1"
        ));

        assertTrue(result.success());
        assertEquals(List.of("a", "b", "c"), result.executedNodeIds());
        assertTrue(executedInLive.isEmpty());
        assertEquals(3, auditPort.entries().size());
        assertTrue(auditPort.entries().stream().allMatch(entry -> entry.eventType().equals("workflow.simulate.node")));
    }

    @Test
    void liveModeExecutesActions() {
        List<String> executedInLive = new ArrayList<>();
        WorkflowActionExecutorPort executor = (definition, node) -> executedInLive.add(node.id());
        WorkflowEngine engine = new WorkflowEngine(new WorkflowValidator(), executor, entry -> {
        });

        WorkflowDefinition definition = new WorkflowDefinition(
                "guild-1",
                "wf-1",
                1,
                List.of(new WorkflowTrigger("t1", TriggerType.DISCORD_EVENT, Map.of("event", "READY"))),
                List.of(new WorkflowNode("a", ActionType.SEND_MESSAGE, Map.of())),
                List.of()
        );

        WorkflowExecutionResult result = engine.execute(new WorkflowExecutionRequest(
                definition,
                WorkflowExecutionMode.LIVE,
                "user-1",
                "corr-1"
        ));

        assertTrue(result.success());
        assertEquals(List.of("a"), executedInLive);
    }
}
