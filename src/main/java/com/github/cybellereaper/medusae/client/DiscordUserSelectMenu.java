package com.github.cybellereaper.medusae.client;

import java.util.LinkedHashMap;
import java.util.Map;

public record DiscordUserSelectMenu(
        String customId,
        String placeholder,
        Integer minValues,
        Integer maxValues,
        boolean disabled
) implements DiscordComponent {
    public DiscordUserSelectMenu {
        customId = DiscordSelectMenuSupport.requireCustomId(customId);
        DiscordSelectMenuSupport.validateSelectionRange(minValues, maxValues);
    }

    public static DiscordUserSelectMenu of(String customId) {
        return new DiscordUserSelectMenu(customId, null, null, null, false);
    }

    public DiscordUserSelectMenu withPlaceholder(String placeholder) {
        return new DiscordUserSelectMenu(customId, placeholder, minValues, maxValues, disabled);
    }

    public DiscordUserSelectMenu withSelectionRange(Integer minValues, Integer maxValues) {
        return new DiscordUserSelectMenu(customId, placeholder, minValues, maxValues, disabled);
    }

    public DiscordUserSelectMenu disable() {
        return disabled ? this : new DiscordUserSelectMenu(customId, placeholder, minValues, maxValues, true);
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", 5);
        DiscordSelectMenuSupport.putSharedPayload(payload, customId, placeholder, minValues, maxValues, disabled);
        return payload;
    }
}
