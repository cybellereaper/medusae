package com.github.cybellereaper.medusae.servicea.infrastructure.workflow;

import com.github.cybellereaper.medusae.servicea.application.workflow.WorkflowAuditEntry;
import com.github.cybellereaper.medusae.servicea.application.workflow.WorkflowAuditPort;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryWorkflowAuditPort implements WorkflowAuditPort {

    private final List<WorkflowAuditEntry> entries = new ArrayList<>();

    @Override
    public void append(WorkflowAuditEntry entry) {
        entries.add(entry);
    }

    public List<WorkflowAuditEntry> entries() {
        return List.copyOf(entries);
    }
}
