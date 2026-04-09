package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.infrastructure.workflow.InMemoryWorkflowAuditPort;
import com.github.cybellereaper.medusae.servicea.infrastructure.workflow.JacksonWorkflowDefinitionParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowPublishServiceTest {

    @Test
    void validatesAndAuditsPublishPayload() {
        InMemoryWorkflowAuditPort auditPort = new InMemoryWorkflowAuditPort();
        WorkflowPublishService service = new WorkflowPublishService(
                new JacksonWorkflowDefinitionParser(),
                new WorkflowValidator(),
                auditPort
        );

        String payload = """
                {
                  "tenantId": "guild-1",
                  "workflowId": "wf-1",
                  "version": 1,
                  "triggers": [{"id":"t1","type":"WEBHOOK","config":{"path":"/hook"}}],
                  "nodes": [{"id":"n1","action":"CREATE_TICKET","config":{}}],
                  "edges": []
                }
                """;

        WorkflowValidationResult result = service.validateAndAudit(
                payload,
                WorkflowPublishService.SourceFormat.JSON,
                "admin-1",
                "corr-123"
        );

        assertTrue(result.valid());
        assertEquals(1, auditPort.entries().size());
        assertEquals("workflow.publish.validation", auditPort.entries().getFirst().eventType());
    }
}
