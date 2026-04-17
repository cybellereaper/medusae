package com.github.cybellereaper.medusae.commands.core.response.component;

public record RoleSelectSpec(String customId, String placeholder, Integer minValues, Integer maxValues,
                             boolean disabled) implements ComponentSpec {
    public static RoleSelectSpec of(String customId) {
        return new RoleSelectSpec(customId, null, null, null, false);
    }

    public RoleSelectSpec withPlaceholder(String value) {
        return new RoleSelectSpec(customId, value, minValues, maxValues, disabled);
    }

    public RoleSelectSpec withRange(Integer min, Integer max) {
        return new RoleSelectSpec(customId, placeholder, min, max, disabled);
    }

    public RoleSelectSpec disable() {
        return disabled ? this : new RoleSelectSpec(customId, placeholder, minValues, maxValues, true);
    }
}
