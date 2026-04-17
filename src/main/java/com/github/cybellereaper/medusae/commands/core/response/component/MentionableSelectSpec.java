package com.github.cybellereaper.medusae.commands.core.response.component;

public record MentionableSelectSpec(String customId, String placeholder, Integer minValues, Integer maxValues,
                                    boolean disabled) implements ComponentSpec {
    public static MentionableSelectSpec of(String customId) {
        return new MentionableSelectSpec(customId, null, null, null, false);
    }

    public MentionableSelectSpec withPlaceholder(String value) {
        return new MentionableSelectSpec(customId, value, minValues, maxValues, disabled);
    }

    public MentionableSelectSpec withRange(Integer min, Integer max) {
        return new MentionableSelectSpec(customId, placeholder, min, max, disabled);
    }

    public MentionableSelectSpec disable() {
        return disabled ? this : new MentionableSelectSpec(customId, placeholder, minValues, maxValues, true);
    }
}
