package com.github.cybellereaper.medusae.servicea.infrastructure.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.ActionType;
import com.github.cybellereaper.medusae.servicea.domain.workflow.TriggerType;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonWorkflowDefinitionParserTest {

    private final JacksonWorkflowDefinitionParser parser = new JacksonWorkflowDefinitionParser();

    @Test
    void parsesJsonWorkflow() {
        String payload = """
                {
                  "tenantId": "guild-1",
                  "workflowId": "wf-1",
                  "version": 1,
                  "triggers": [{"id":"t1","type":"DISCORD_EVENT","config":{"event":"MESSAGE_CREATE"}}],
                  "nodes": [{"id":"n1","action":"SEND_MESSAGE","config":{"channelId":"123"}}],
                  "edges": []
                }
                """;

        WorkflowDefinition definition = parser.parseJson(payload);

        assertEquals("guild-1", definition.tenantId());
        assertEquals(TriggerType.DISCORD_EVENT, definition.triggers().getFirst().type());
        assertEquals(ActionType.SEND_MESSAGE, definition.nodes().getFirst().action());
    }

    @Test
    void parsesYamlWorkflow() {
        String payload = """
                tenantId: guild-1
                workflowId: wf-1
                version: 1
                triggers:
                  - id: t1
                    type: CRON
                    config:
                      expression: \"0 */10 * * * *\"
                nodes:
                  - id: n1
                    action: CALL_WEBHOOK
                    config:
                      url: https://example.test/hook
                edges: []
                """;

        WorkflowDefinition definition = parser.parseYaml(payload);

        assertEquals("wf-1", definition.workflowId());
        assertEquals(TriggerType.CRON, definition.triggers().getFirst().type());
        assertEquals(ActionType.CALL_WEBHOOK, definition.nodes().getFirst().action());
    }
}
