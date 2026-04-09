package com.github.cybellereaper.medusae.servicea.application.tenant;

import com.github.cybellereaper.medusae.commands.core.model.CommandInteraction;
import com.github.cybellereaper.medusae.commands.core.model.InteractionExecution;

/**
 * Application boundary port for tenant scoping checks.
 */
public interface TenantBoundaryGuard {

    void enforceCommandBoundary(CommandInteraction interaction);

    void enforceInteractionBoundary(InteractionExecution interaction);
}
