package com.github.cybellereaper.medusae.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record DiscordChannelSelectMenu(
        String customId,
        String placeholder,
        Integer minValues,
        Integer maxValues,
        boolean disabled,
        List<Integer> channelTypes
) implements DiscordComponent {
    public DiscordChannelSelectMenu {
        customId = DiscordSelectMenuSupport.requireCustomId(customId);
        DiscordSelectMenuSupport.validateSelectionRange(minValues, maxValues);

        if (channelTypes != null) {
            channelTypes = channelTypes.stream().filter(Objects::nonNull).toList();
            if (channelTypes.isEmpty()) {
                channelTypes = null;
            }
        }
    }

    public static DiscordChannelSelectMenu of(String customId) {
        return new DiscordChannelSelectMenu(customId, null, null, null, false, null);
    }

    public DiscordChannelSelectMenu withPlaceholder(String placeholder) {
        return new DiscordChannelSelectMenu(customId, placeholder, minValues, maxValues, disabled, channelTypes);
    }

    public DiscordChannelSelectMenu withSelectionRange(Integer minValues, Integer maxValues) {
        return new DiscordChannelSelectMenu(customId, placeholder, minValues, maxValues, disabled, channelTypes);
    }

    public DiscordChannelSelectMenu withChannelTypes(List<Integer> channelTypes) {
        return new DiscordChannelSelectMenu(customId, placeholder, minValues, maxValues, disabled, channelTypes);
    }

    public DiscordChannelSelectMenu disable() {
        return disabled ? this : new DiscordChannelSelectMenu(customId, placeholder, minValues, maxValues, true, channelTypes);
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", 8);
        DiscordSelectMenuSupport.putSharedPayload(payload, customId, placeholder, minValues, maxValues, disabled);
        if (channelTypes != null && !channelTypes.isEmpty()) {
            payload.put("channel_types", channelTypes);
        }
        return payload;
    }
}
