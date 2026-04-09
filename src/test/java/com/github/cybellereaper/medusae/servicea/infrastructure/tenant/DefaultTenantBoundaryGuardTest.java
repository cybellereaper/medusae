package com.github.cybellereaper.medusae.servicea.infrastructure.tenant;

import com.github.cybellereaper.medusae.commands.core.exception.CheckFailedException;
import com.github.cybellereaper.medusae.commands.core.model.*;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultTenantBoundaryGuardTest {

    private final DefaultTenantBoundaryGuard guard = new DefaultTenantBoundaryGuard();

    @Test
    void allowsDmCommandWithoutGuildId() {
        CommandInteraction interaction = new CommandInteraction(
                "ping", CommandType.CHAT_INPUT, null, null,
                Map.of(), null, null, true, null, "user-1", Set.of(), Set.of(),
                null, null, null, null, null, null
        );

        assertDoesNotThrow(() -> guard.enforceCommandBoundary(interaction));
    }

    @Test
    void rejectsGuildCommandWithoutGuildId() {
        CommandInteraction interaction = new CommandInteraction(
                "ping", CommandType.CHAT_INPUT, null, null,
                Map.of(), null, null, false, null, "user-1", Set.of(), Set.of(),
                null, null, null, null, null, null
        );

        assertThrows(CheckFailedException.class, () -> guard.enforceCommandBoundary(interaction));
    }

    @Test
    void rejectsGuildInteractionWithoutGuildId() {
        InteractionExecution execution = new InteractionExecution(
                InteractionHandlerType.BUTTON,
                "ticket:create",
                Map.of(),
                null,
                ResolvedEntities.empty(),
                false,
                null,
                "user-1",
                Set.of(),
                Set.of(),
                null
        );

        assertThrows(CheckFailedException.class, () -> guard.enforceInteractionBoundary(execution));
    }
}
