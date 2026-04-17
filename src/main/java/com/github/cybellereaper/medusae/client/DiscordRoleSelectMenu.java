package com.github.cybellereaper.medusae.client;

import java.util.LinkedHashMap;
import java.util.Map;

public record DiscordRoleSelectMenu(
        String customId,
        String placeholder,
        Integer minValues,
        Integer maxValues,
        boolean disabled
) implements DiscordComponent {
    public DiscordRoleSelectMenu {
        customId = DiscordSelectMenuSupport.requireCustomId(customId);
        DiscordSelectMenuSupport.validateSelectionRange(minValues, maxValues);
    }

    public static DiscordRoleSelectMenu of(String customId) {
        return new DiscordRoleSelectMenu(customId, null, null, null, false);
    }

    public DiscordRoleSelectMenu withPlaceholder(String placeholder) {
        return new DiscordRoleSelectMenu(customId, placeholder, minValues, maxValues, disabled);
    }

    public DiscordRoleSelectMenu withSelectionRange(Integer minValues, Integer maxValues) {
        return new DiscordRoleSelectMenu(customId, placeholder, minValues, maxValues, disabled);
    }

    public DiscordRoleSelectMenu disable() {
        return disabled ? this : new DiscordRoleSelectMenu(customId, placeholder, minValues, maxValues, true);
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", 6);
        DiscordSelectMenuSupport.putSharedPayload(payload, customId, placeholder, minValues, maxValues, disabled);
        return payload;
    }
}
