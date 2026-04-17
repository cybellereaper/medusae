package com.github.cybellereaper.medusae.commands.core.response.component;

public record UserSelectSpec(String customId, String placeholder, Integer minValues, Integer maxValues,
                             boolean disabled) implements ComponentSpec {
    public static UserSelectSpec of(String customId) {
        return new UserSelectSpec(customId, null, null, null, false);
    }

    public UserSelectSpec withPlaceholder(String value) {
        return new UserSelectSpec(customId, value, minValues, maxValues, disabled);
    }

    public UserSelectSpec withRange(Integer min, Integer max) {
        return new UserSelectSpec(customId, placeholder, min, max, disabled);
    }

    public UserSelectSpec disable() {
        return disabled ? this : new UserSelectSpec(customId, placeholder, minValues, maxValues, true);
    }
}
