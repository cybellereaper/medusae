package com.github.cybellereaper.medusae.servicea.application.workflow;

import com.github.cybellereaper.medusae.servicea.domain.workflow.TriggerType;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;

public interface WorkflowTriggerAdapter {

    TriggerType type();

    void register(WorkflowDefinition definition);
}
