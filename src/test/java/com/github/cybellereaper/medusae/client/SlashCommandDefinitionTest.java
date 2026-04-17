package com.github.cybellereaper.medusae.client;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SlashCommandDefinitionTest {
    @Test
    void buildsPayloadWithOptions() {
        SlashCommandDefinition command = new SlashCommandDefinition(
                "echo",
                "Echo text",
                List.of(SlashCommandOptionDefinition.autocompletedString("text", "Text to echo", true))
        );

        Map<String, Object> payload = command.toRequestPayload();

        assertEquals(1, payload.get("type"));
        assertEquals("echo", payload.get("name"));
        assertTrue(payload.containsKey("options"));
    }

    @Test
    void buildsContextMenuPayloadWithoutDescriptionAndOptions() {
        SlashCommandDefinition command = SlashCommandDefinition.userContextMenu("Inspect User")
                .withDmPermission(false)
                .withNsfw(false);

        Map<String, Object> payload = command.toRequestPayload();

        assertEquals(2, payload.get("type"));
        assertFalse(payload.containsKey("description"));
        assertFalse(payload.containsKey("options"));
        assertEquals(false, payload.get("dm_permission"));
    }

    @Test
    void supportsDefaultMemberPermissions() {
        SlashCommandDefinition command = SlashCommandDefinition.simple("secure", "Secure command")
                .withDefaultMemberPermissions(DiscordPermissions.of(
                        DiscordPermissions.SEND_MESSAGES,
                        DiscordPermissions.USE_APPLICATION_COMMANDS
                ));

        Map<String, Object> payload = command.toRequestPayload();
        assertTrue(payload.containsKey("default_member_permissions"));
    }

    @Test
    void validatesRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> SlashCommandDefinition.simple("", "desc"));
        assertThrows(IllegalArgumentException.class, () -> SlashCommandDefinition.simple("ping", ""));
        assertThrows(IllegalArgumentException.class, () -> SlashCommandOptionDefinition.string("", "text", true));
        assertThrows(IllegalArgumentException.class,
                () -> new SlashCommandDefinition(SlashCommandDefinition.USER, "menu", "desc", List.of(), null, null, null, Map.of(), Map.of(), List.of()));
    }

    @Test
    void supportsLocalizationsAndContextsInPayload() {
        SlashCommandDefinition command = SlashCommandDefinition.simple("settings", "Settings")
                .withNameLocalizations(Map.of("en-US", "Settings"))
                .withDescriptionLocalizations(Map.of("en-US", "Manage settings"))
                .withContexts(List.of(0, 2));

        Map<String, Object> payload = command.toRequestPayload();

        assertEquals(Map.of("en-US", "Settings"), payload.get("name_localizations"));
        assertEquals(Map.of("en-US", "Manage settings"), payload.get("description_localizations"));
        assertEquals(List.of(0, 2), payload.get("contexts"));
    }

    @Test
    void embedPayloadOmitsBlankFields() {
        DiscordEmbed embed = new DiscordEmbed("", "Some description", null);

        Map<String, Object> payload = embed.toPayload();

        assertFalse(payload.containsKey("title"));
        assertEquals("Some description", payload.get("description"));
    }

    @Test
    void autocompleteChoiceValidatesInput() {
        assertThrows(IllegalArgumentException.class, () -> new AutocompleteChoice("", "value"));
        assertThrows(IllegalArgumentException.class, () -> new AutocompleteChoice("name", ""));
    }
}
