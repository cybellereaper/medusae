package com.github.cybellereaper.medusae.servicea.infrastructure.workflow;

import com.github.cybellereaper.medusae.servicea.application.workflow.WorkflowTriggerAdapter;
import com.github.cybellereaper.medusae.servicea.domain.workflow.TriggerType;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;

public final class CronTriggerAdapter implements WorkflowTriggerAdapter {

    @Override
    public TriggerType type() {
        return TriggerType.CRON;
    }

    @Override
    public void register(WorkflowDefinition definition) {
        // Scaffold adapter for scheduler integration.
    }
}
