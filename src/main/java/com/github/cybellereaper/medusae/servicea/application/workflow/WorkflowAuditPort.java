package com.github.cybellereaper.medusae.servicea.application.workflow;

public interface WorkflowAuditPort {

    void append(WorkflowAuditEntry entry);
}
