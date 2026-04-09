package com.github.cybellereaper.medusae.servicea.infrastructure.workflow;

import com.github.cybellereaper.medusae.servicea.application.workflow.WorkflowTriggerAdapter;
import com.github.cybellereaper.medusae.servicea.domain.workflow.TriggerType;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;

public final class WebhookTriggerAdapter implements WorkflowTriggerAdapter {

    @Override
    public TriggerType type() {
        return TriggerType.WEBHOOK;
    }

    @Override
    public void register(WorkflowDefinition definition) {
        // Scaffold adapter for webhook/event ingress integration.
    }
}
