package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowValidatorTest {

    private final WorkflowValidator validator = new WorkflowValidator();

    @Test
    void rejectsUnknownEdgeNodeAndDuplicateNodeId() {
        WorkflowDefinition definition = new WorkflowDefinition(
                "guild-1",
                "wf-1",
                1,
                List.of(new WorkflowTrigger("t1", TriggerType.DISCORD_EVENT, Map.of("event", "MESSAGE_CREATE"))),
                List.of(
                        new WorkflowNode("n1", ActionType.SEND_MESSAGE, Map.of()),
                        new WorkflowNode("n1", ActionType.CREATE_TICKET, Map.of())
                ),
                List.of(new WorkflowEdge("n1", "n2"))
        );

        WorkflowValidationResult result = validator.validate(definition);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(error -> error.message().contains("duplicate node id")));
        assertTrue(result.errors().stream().anyMatch(error -> error.message().contains("unknown to node")));
    }

    @Test
    void acceptsWellFormedWorkflowDefinition() {
        WorkflowDefinition definition = new WorkflowDefinition(
                "guild-1",
                "wf-1",
                1,
                List.of(new WorkflowTrigger("t1", TriggerType.CRON, Map.of("expression", "0 */5 * * * *"))),
                List.of(
                        new WorkflowNode("n1", ActionType.SEND_MESSAGE, Map.of()),
                        new WorkflowNode("n2", ActionType.CALL_WEBHOOK, Map.of())
                ),
                List.of(new WorkflowEdge("n1", "n2"))
        );

        WorkflowValidationResult result = validator.validate(definition);

        assertTrue(result.valid());
    }
}
