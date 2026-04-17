package com.github.cybellereaper.medusae.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record DiscordStringSelectMenu(
        String customId,
        List<DiscordSelectOption> options,
        String placeholder,
        Integer minValues,
        Integer maxValues,
        boolean disabled
) implements DiscordComponent {
    public DiscordStringSelectMenu {
        customId = DiscordSelectMenuSupport.requireCustomId(customId);

        Objects.requireNonNull(options, "options");
        options = options.stream().filter(Objects::nonNull).toList();
        if (options.isEmpty()) {
            throw new IllegalArgumentException("options must not be empty");
        }

        DiscordSelectMenuSupport.validateSelectionRange(minValues, maxValues);
    }

    public static DiscordStringSelectMenu of(String customId, List<DiscordSelectOption> options) {
        return new DiscordStringSelectMenu(customId, options, null, null, null, false);
    }

    public DiscordStringSelectMenu withPlaceholder(String placeholder) {
        return new DiscordStringSelectMenu(customId, options, placeholder, minValues, maxValues, disabled);
    }

    public DiscordStringSelectMenu withSelectionRange(Integer minValues, Integer maxValues) {
        return new DiscordStringSelectMenu(customId, options, placeholder, minValues, maxValues, disabled);
    }

    public DiscordStringSelectMenu disable() {
        return disabled ? this : new DiscordStringSelectMenu(customId, options, placeholder, minValues, maxValues, true);
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", 3);
        DiscordSelectMenuSupport.putSharedPayload(payload, customId, placeholder, minValues, maxValues, disabled);
        payload.put("options", options.stream().map(DiscordSelectOption::toPayload).toList());
        return payload;
    }
}
