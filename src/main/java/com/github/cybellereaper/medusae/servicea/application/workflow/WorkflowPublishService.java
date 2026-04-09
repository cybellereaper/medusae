package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public final class WorkflowPublishService {

    public enum SourceFormat { JSON, YAML }

    private final WorkflowDefinitionParser parser;
    private final WorkflowValidator validator;
    private final WorkflowAuditPort auditPort;

    public WorkflowPublishService(WorkflowDefinitionParser parser,
                                  WorkflowValidator validator,
                                  WorkflowAuditPort auditPort) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.auditPort = Objects.requireNonNull(auditPort, "auditPort");
    }

    public WorkflowValidationResult validateAndAudit(String payload,
                                                     SourceFormat sourceFormat,
                                                     String actorUserId,
                                                     String correlationId) {
        WorkflowDefinition definition = sourceFormat == SourceFormat.JSON
                ? parser.parseJson(payload)
                : parser.parseYaml(payload);

        WorkflowValidationResult result = validator.validate(definition);
        auditPort.append(new WorkflowAuditEntry(
                definition.tenantId(),
                definition.workflowId(),
                "workflow.publish.validation",
                actorUserId,
                correlationId,
                Instant.now(),
                Map.of("valid", String.valueOf(result.valid()), "errorCount", String.valueOf(result.errors().size()))
        ));

        return result;
    }
}
