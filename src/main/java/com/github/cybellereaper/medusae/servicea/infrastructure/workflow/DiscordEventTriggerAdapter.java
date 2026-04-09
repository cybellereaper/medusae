package com.github.cybellereaper.medusae.servicea.infrastructure.workflow;

import com.github.cybellereaper.medusae.servicea.application.workflow.WorkflowTriggerAdapter;
import com.github.cybellereaper.medusae.servicea.domain.workflow.TriggerType;
import com.github.cybellereaper.medusae.servicea.domain.workflow.WorkflowDefinition;

public final class DiscordEventTriggerAdapter implements WorkflowTriggerAdapter {

    @Override
    public TriggerType type() {
        return TriggerType.DISCORD_EVENT;
    }

    @Override
    public void register(WorkflowDefinition definition) {
        // Scaffold adapter for Service A gateway event integration.
    }
}
