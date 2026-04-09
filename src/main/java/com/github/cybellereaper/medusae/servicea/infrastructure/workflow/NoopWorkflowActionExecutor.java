package com.github.cybellereaper.medusae.servicea.infrastructure.workflow;

import com.github.cybellereaper.medusae.servicea.application.workflow.WorkflowActionExecutorPort;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowNode;

public final class NoopWorkflowActionExecutor implements WorkflowActionExecutorPort {

    @Override
    public void execute(WorkflowDefinition definition, WorkflowNode node) {
        // no-op scaffold adapter
    }
}
