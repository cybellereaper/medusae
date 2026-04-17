package com.github.cybellereaper.medusae.commands.core.response.component;

import java.util.List;

public record ChannelSelectSpec(String customId, String placeholder, Integer minValues, Integer maxValues,
                                boolean disabled, List<Integer> channelTypes) implements ComponentSpec {
    public static ChannelSelectSpec of(String customId) {
        return new ChannelSelectSpec(customId, null, null, null, false, null);
    }

    public ChannelSelectSpec withPlaceholder(String value) {
        return new ChannelSelectSpec(customId, value, minValues, maxValues, disabled, channelTypes);
    }

    public ChannelSelectSpec withRange(Integer min, Integer max) {
        return new ChannelSelectSpec(customId, placeholder, min, max, disabled, channelTypes);
    }

    public ChannelSelectSpec withChannelTypes(List<Integer> value) {
        return new ChannelSelectSpec(customId, placeholder, minValues, maxValues, disabled, value);
    }

    public ChannelSelectSpec disable() {
        return disabled ? this : new ChannelSelectSpec(customId, placeholder, minValues, maxValues, true, channelTypes);
    }
}
