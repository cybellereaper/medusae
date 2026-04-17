package com.github.cybellereaper.medusae.client;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DiscordComponentsTest {
    @Test
    void messagePayloadIncludesButtonsAndSelectMenus() {
        DiscordActionRow buttonRow = DiscordActionRow.of(List.of(
                DiscordButton.primary("confirm", "Confirm"),
                DiscordButton.link("https://discord.com", "Open Docs")
        ));

        DiscordActionRow selectRow = DiscordActionRow.of(List.of(
                DiscordStringSelectMenu.of("theme", List.of(
                                DiscordSelectOption.of("Light", "light"),
                                DiscordSelectOption.of("Dark", "dark").asDefault()
                        ))
                        .withPlaceholder("Select theme")
                        .withSelectionRange(1, 1)
        ));

        Map<String, Object> payload = DiscordMessage.ofContent("Choose one")
                .withComponents(List.of(buttonRow, selectRow))
                .toPayload();

        assertTrue(payload.containsKey("components"));
        List<?> components = (List<?>) payload.get("components");
        assertEquals(2, components.size());
    }

    @Test
    void buttonValidationRejectsInvalidLinkButton() {
        assertThrows(IllegalArgumentException.class,
                () -> new DiscordButton(DiscordButton.LINK, "Docs", "id", null, null, false));
    }

    @Test
    void selectMenuValidationRejectsEmptyOptions() {
        assertThrows(IllegalArgumentException.class,
                () -> DiscordStringSelectMenu.of("theme", List.of()));
    }

    @Test
    void entitySelectMenusSerializeExpectedDiscordTypes() {
        DiscordActionRow selectRow = DiscordActionRow.of(List.of(
                DiscordUserSelectMenu.of("assignee").withPlaceholder("Choose user").withSelectionRange(1, 1),
                DiscordRoleSelectMenu.of("roles").withSelectionRange(0, 3),
                DiscordMentionableSelectMenu.of("mentions").disable(),
                DiscordChannelSelectMenu.of("channels").withChannelTypes(List.of(0, 2)).withSelectionRange(1, 2)
        ));

        Map<String, Object> payload = DiscordMessage.ofContent("Assign")
                .withComponents(List.of(selectRow))
                .toPayload();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) payload.get("components");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> components = (List<Map<String, Object>>) rows.get(0).get("components");

        assertEquals(5, components.get(0).get("type"));
        assertEquals(6, components.get(1).get("type"));
        assertEquals(7, components.get(2).get("type"));
        assertEquals(8, components.get(3).get("type"));
        assertEquals(List.of(0, 2), components.get(3).get("channel_types"));
    }

    @Test
    void modalPayloadIncludesTextInputs() {
        DiscordModal modal = DiscordModal.of(
                "feedback_modal",
                "Feedback",
                List.of(DiscordActionRow.of(List.of(
                        DiscordTextInput.shortInput("summary", "Summary")
                                .withLengthRange(1, 100)
                                .withPlaceholder("Share quick feedback")
                )))
        );

        Map<String, Object> payload = modal.toPayload();

        assertEquals("feedback_modal", payload.get("custom_id"));
        assertTrue(payload.containsKey("components"));
    }

    @Test
    void modalValidationRejectsNonTextInputComponents() {
        assertThrows(IllegalArgumentException.class,
                () -> DiscordModal.of("id", "Title", List.of(
                        DiscordActionRow.of(List.of(DiscordButton.primary("a", "b")))
                )));
    }
}
