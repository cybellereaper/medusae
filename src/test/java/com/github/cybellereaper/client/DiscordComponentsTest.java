package com.github.cybellereaper.client;

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
}
