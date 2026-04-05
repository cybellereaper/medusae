package com.github.cybellereaper.client;

import java.util.LinkedHashMap;
import java.util.Map;

public record DiscordEmbed(
        String title,
        String description,
        Integer color
) {
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();

        if (title != null && !title.isBlank()) {
            payload.put("title", title);
        }
        if (description != null && !description.isBlank()) {
            payload.put("description", description);
        }
        if (color != null) {
            payload.put("color", color);
        }

        return payload;
    }
}
