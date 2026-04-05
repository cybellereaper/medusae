package com.github.cybellereaper.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SlashCommandDefinition(
        String name,
        String description,
        List<SlashCommandOptionDefinition> options
) {
    public SlashCommandDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        options = options == null ? List.of() : List.copyOf(options);

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
    }

    public static SlashCommandDefinition simple(String name, String description) {
        return new SlashCommandDefinition(name, description, List.of());
    }

    public Map<String, Object> toRequestPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", 1);
        payload.put("name", name);
        payload.put("description", description);

        if (!options.isEmpty()) {
            payload.put("options", options.stream().map(SlashCommandOptionDefinition::toRequestPayload).toList());
        }

        return payload;
    }
}
