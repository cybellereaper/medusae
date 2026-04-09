package com.github.cybellereaper.medusae.commands.core.tenant;

import com.github.cybellereaper.medusae.commands.core.exception.CheckFailedException;

/**
 * Centralized tenant (guild) boundary guard used by command and interaction execution.
 */
public final class TenantGuard {

    private TenantGuard() {
    }

    public static void requireGuildContext(boolean dm, String guildId, String operation) {
        if (dm) {
            return;
        }
        if (guildId == null || guildId.isBlank()) {
            throw new CheckFailedException("Missing guild context for " + operation);
        }
    }
}
