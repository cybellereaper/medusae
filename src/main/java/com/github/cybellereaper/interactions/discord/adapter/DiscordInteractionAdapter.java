package com.github.cybellereaper.interactions.discord.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.cybellereaper.interactions.core.model.InteractionPayload;
import com.github.cybellereaper.interactions.core.model.InteractionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiscordInteractionAdapter {
    public InteractionPayload map(JsonNode interaction) {
        JsonNode data = interaction.path("data");
        InteractionType type = mapType(interaction.path("type").asInt(0), data.path("component_type").asInt(0));

        List<String> selected = new ArrayList<>();
        data.path("values").forEach(node -> selected.add(node.asText()));

        Map<String, String> fields = new HashMap<>();
        data.path("components").forEach(row -> row.path("components").forEach(component ->
                fields.put(component.path("custom_id").asText(), component.path("value").asText())));

        return new InteractionPayload(
                type,
                data.path("custom_id").asText(null),
                textOrNull(interaction.path("member").path("user").path("id"), interaction.path("user").path("id")),
                textOrNull(interaction.path("member").path("user").path("id")),
                textOrNull(interaction.path("guild_id")),
                textOrNull(interaction.path("channel_id")),
                textOrNull(interaction.path("message").path("id")),
                selected,
                fields,
                interaction
        );
    }

    private static String textOrNull(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && node.isTextual()) {
                String value = node.asText();
                if (!value.isBlank()) return value;
            }
        }
        return null;
    }

    private static InteractionType mapType(int interactionType, int componentType) {
        if (interactionType == 5) return InteractionType.MODAL;
        if (interactionType != 3) {
            throw new IllegalArgumentException("Unsupported interaction type: " + interactionType);
        }
        return switch (componentType) {
            case 2 -> InteractionType.BUTTON;
            case 3 -> InteractionType.STRING_SELECT;
            case 5 -> InteractionType.USER_SELECT;
            case 6 -> InteractionType.ROLE_SELECT;
            case 7 -> InteractionType.MENTIONABLE_SELECT;
            case 8 -> InteractionType.CHANNEL_SELECT;
            default -> throw new IllegalArgumentException("Unsupported component type: " + componentType);
        };
    }
}
