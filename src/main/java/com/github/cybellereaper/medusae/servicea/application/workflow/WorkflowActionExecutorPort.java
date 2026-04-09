package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowNode;

public interface WorkflowActionExecutorPort {

    void execute(WorkflowDefinition definition, WorkflowNode node);
}
