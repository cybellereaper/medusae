package com.github.cybellereaper.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandRegistrationTest {
    @Test
    void buildsSlashCommandRegistrationWithAutocompleteHandlers() {
        CommandRegistration registration = CommandRegistration.builder(
                        SlashCommandDefinition.simple("ping", "Reply with pong"))
                .onSlash(interaction -> {})
                .onAutocomplete(interaction -> {})
                .build();

        assertEquals("ping", registration.name());
        assertEquals(SlashCommandDefinition.CHAT_INPUT, registration.definition().type());
    }

    @Test
    void rejectsSlashHandlersForUserContextMenu() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CommandRegistration.builder(SlashCommandDefinition.userContextMenu("Inspect User"))
                        .onSlash(interaction -> {})
                        .build()
        );

        assertEquals("Context menu commands cannot declare slash/autocomplete handlers", exception.getMessage());
    }

    @Test
    void rejectsUserContextHandlersForMessageContextMenu() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CommandRegistration.builder(SlashCommandDefinition.messageContextMenu("Quote"))
                        .onUserContextMenu(interaction -> {})
                        .build()
        );

        assertEquals("Message context menu commands cannot declare user context handlers", exception.getMessage());
    }

    @Test
    void allowsTypedSlashHandlerForChatInputCommand() {
        assertDoesNotThrow(() -> CommandRegistration.builder(SlashCommandDefinition.simple("echo", "Echo"))
                .onSlashContext(interaction -> {})
                .build());
    }
}
