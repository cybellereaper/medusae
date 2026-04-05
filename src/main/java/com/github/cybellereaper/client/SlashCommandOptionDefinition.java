package com.github.cybellereaper.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record SlashCommandOptionDefinition(
        int type,
        String name,
        String description,
        boolean required,
        boolean autocomplete
) {
    public static final int STRING = 3;
    public static final int INTEGER = 4;
    public static final int BOOLEAN = 5;
    public static final int USER = 6;
    public static final int CHANNEL = 7;
    public static final int ROLE = 8;
    public static final int MENTIONABLE = 9;
    public static final int NUMBER = 10;
    public static final int ATTACHMENT = 11;

    public SlashCommandOptionDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");

        if (type < STRING || type > ATTACHMENT) {
            throw new IllegalArgumentException("Unsupported slash command option type: " + type);
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
    }

    public static SlashCommandOptionDefinition string(String name, String description, boolean required) {
        return new SlashCommandOptionDefinition(STRING, name, description, required, false);
    }

    public static SlashCommandOptionDefinition autocompletedString(String name, String description, boolean required) {
        return new SlashCommandOptionDefinition(STRING, name, description, required, true);
    }

    Map<String, Object> toRequestPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", type);
        payload.put("name", name);
        payload.put("description", description);
        payload.put("required", required);
        if (autocomplete) {
            payload.put("autocomplete", true);
        }
        return payload;
    }
}
