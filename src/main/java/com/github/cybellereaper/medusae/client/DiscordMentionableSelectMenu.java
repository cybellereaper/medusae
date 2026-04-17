package com.github.cybellereaper.medusae.client;

import java.util.LinkedHashMap;
import java.util.Map;

public record DiscordMentionableSelectMenu(
        String customId,
        String placeholder,
        Integer minValues,
        Integer maxValues,
        boolean disabled
) implements DiscordComponent {
    public DiscordMentionableSelectMenu {
        customId = DiscordSelectMenuSupport.requireCustomId(customId);
        DiscordSelectMenuSupport.validateSelectionRange(minValues, maxValues);
    }

    public static DiscordMentionableSelectMenu of(String customId) {
        return new DiscordMentionableSelectMenu(customId, null, null, null, false);
    }

    public DiscordMentionableSelectMenu withPlaceholder(String placeholder) {
        return new DiscordMentionableSelectMenu(customId, placeholder, minValues, maxValues, disabled);
    }

    public DiscordMentionableSelectMenu withSelectionRange(Integer minValues, Integer maxValues) {
        return new DiscordMentionableSelectMenu(customId, placeholder, minValues, maxValues, disabled);
    }

    public DiscordMentionableSelectMenu disable() {
        return disabled ? this : new DiscordMentionableSelectMenu(customId, placeholder, minValues, maxValues, true);
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", 7);
        DiscordSelectMenuSupport.putSharedPayload(payload, customId, placeholder, minValues, maxValues, disabled);
        return payload;
    }
}
