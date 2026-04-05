package com.github.cybellereaper.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parsed slash-command option set with typed accessors.
 */
public final class SlashCommandParameters {
    private final Map<String, JsonNode> optionsByName;
    private final Map<String, JsonNode> resolvedUsersById;
    private final Map<String, JsonNode> resolvedChannelsById;
    private final Map<String, JsonNode> resolvedAttachmentsById;

    SlashCommandParameters(JsonNode interaction) {
        Objects.requireNonNull(interaction, "interaction");
        JsonNode data = interaction.path("data");
        this.optionsByName = readOptions(data.path("options"));
        JsonNode resolved = data.path("resolved");
        this.resolvedUsersById = readResolvedObjects(resolved.path("users"));
        this.resolvedChannelsById = readResolvedObjects(resolved.path("channels"));
        this.resolvedAttachmentsById = readResolvedObjects(resolved.path("attachments"));
    }

    public String getString(String optionName) {
        JsonNode value = getOptionNode(optionName);
        return value == null ? null : value.asText();
    }

    public Long getLong(String optionName) {
        JsonNode value = getOptionNode(optionName);
        if (value == null || !value.isNumber()) {
            return null;
        }
        return value.longValue();
    }

    public Double getDouble(String optionName) {
        JsonNode value = getOptionNode(optionName);
        if (value == null || !value.isNumber()) {
            return null;
        }
        return value.doubleValue();
    }

    public Boolean getBoolean(String optionName) {
        JsonNode value = getOptionNode(optionName);
        if (value == null || !value.isBoolean()) {
            return null;
        }
        return value.booleanValue();
    }

    /**
     * Returns a snowflake id string for options that resolve to ids (user/channel/attachment/role/mentionable),
     * or null when absent.
     */
    public String getId(String optionName) {
        JsonNode value = getOptionNode(optionName);
        if (value == null) {
            return null;
        }
        String raw = value.asText();
        return raw == null || raw.isBlank() ? null : raw;
    }

    public JsonNode getResolvedUser(String optionName) {
        return getResolvedEntity(getId(optionName), resolvedUsersById);
    }

    public JsonNode getResolvedChannel(String optionName) {
        return getResolvedEntity(getId(optionName), resolvedChannelsById);
    }

    public JsonNode getResolvedAttachment(String optionName) {
        return getResolvedEntity(getId(optionName), resolvedAttachmentsById);
    }

    public String requireString(String optionName) {
        String value = getString(optionName);
        if (value == null) {
            throw new IllegalArgumentException("Missing required string option: " + optionName);
        }
        return value;
    }

    private JsonNode getOptionNode(String optionName) {
        Objects.requireNonNull(optionName, "optionName");
        if (optionName.isBlank()) {
            throw new IllegalArgumentException("optionName must not be blank");
        }
        return optionsByName.get(optionName);
    }

    private static Map<String, JsonNode> readOptions(JsonNode optionsNode) {
        if (!optionsNode.isArray()) {
            return Map.of();
        }

        Map<String, JsonNode> optionMap = new HashMap<>();
        for (JsonNode option : optionsNode) {
            String optionName = option.path("name").asText("");
            if (optionName.isBlank()) {
                continue;
            }

            JsonNode value = option.path("value");
            if (!value.isMissingNode() && !value.isNull()) {
                optionMap.put(optionName, value);
            }
        }

        return Map.copyOf(optionMap);
    }

    private static Map<String, JsonNode> readResolvedObjects(JsonNode resolvedNode) {
        if (!resolvedNode.isObject()) {
            return Map.of();
        }

        Map<String, JsonNode> resolvedById = new HashMap<>();
        resolvedNode.fields().forEachRemaining(entry -> {
            String id = entry.getKey();
            JsonNode value = entry.getValue();
            if (!id.isBlank() && value != null && !value.isNull()) {
                resolvedById.put(id, value);
            }
        });
        return Map.copyOf(resolvedById);
    }

    private static JsonNode getResolvedEntity(String entityId, Map<String, JsonNode> resolvedById) {
        if (entityId == null) {
            return null;
        }
        return resolvedById.get(entityId);
    }
}
