package com.github.cybellereaper.medusae.client;

import java.util.Map;

final class DiscordSelectMenuSupport {
    private DiscordSelectMenuSupport() {
    }

    static String requireCustomId(String customId) {
        java.util.Objects.requireNonNull(customId, "customId");
        if (customId.isBlank()) {
            throw new IllegalArgumentException("customId must not be blank");
        }
        return customId;
    }

    static void validateSelectionRange(Integer minValues, Integer maxValues) {
        if (minValues != null && minValues < 0) {
            throw new IllegalArgumentException("minValues must be >= 0");
        }
        if (maxValues != null && maxValues < 1) {
            throw new IllegalArgumentException("maxValues must be >= 1");
        }
        if (minValues != null && maxValues != null && minValues > maxValues) {
            throw new IllegalArgumentException("minValues cannot be greater than maxValues");
        }
    }

    static void putSharedPayload(Map<String, Object> payload,
                                 String customId,
                                 String placeholder,
                                 Integer minValues,
                                 Integer maxValues,
                                 boolean disabled) {
        payload.put("custom_id", customId);
        if (placeholder != null && !placeholder.isBlank()) {
            payload.put("placeholder", placeholder);
        }
        if (minValues != null) {
            payload.put("min_values", minValues);
        }
        if (maxValues != null) {
            payload.put("max_values", maxValues);
        }
        if (disabled) {
            payload.put("disabled", true);
        }
    }
}
