package com.github.cybellereaper.medusae.servicea.infrastructure.tenant;

import com.github.cybellereaper.medusae.commands.core.exception.CheckFailedException;
import com.github.cybellereaper.medusae.commands.core.model.CommandInteraction;
import com.github.cybellereaper.medusae.commands.core.model.InteractionExecution;
import com.github.cybellereaper.medusae.servicea.application.tenant.TenantBoundaryGuard;
import com.github.cybellereaper.medusae.servicea.domain.tenant.TenantId;

/**
 * Default adapter enforcing tenant isolation for command and interaction execution.
 */
public final class DefaultTenantBoundaryGuard implements TenantBoundaryGuard {

    private static final String COMMAND_OPERATION = "command execution";
    private static final String INTERACTION_OPERATION = "interaction execution";

    @Override
    public void enforceCommandBoundary(CommandInteraction interaction) {
        requireGuildContext(interaction.dm(), interaction.guildId(), COMMAND_OPERATION);
    }

    @Override
    public void enforceInteractionBoundary(InteractionExecution interaction) {
        requireGuildContext(interaction.dm(), interaction.guildId(), INTERACTION_OPERATION);
    }

    private void requireGuildContext(boolean dm, String guildId, String operation) {
        if (dm) {
            return;
        }

        TenantId tenantId = TenantId.fromNullable(guildId);
        if (tenantId == null) {
            throw new CheckFailedException("Missing guild context for " + operation);
        }
    }
}
