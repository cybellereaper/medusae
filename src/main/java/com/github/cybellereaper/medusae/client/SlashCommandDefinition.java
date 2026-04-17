package com.github.cybellereaper.medusae.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SlashCommandDefinition(
        int type,
        String name,
        String description,
        List<SlashCommandOptionDefinition> options,
        String defaultMemberPermissions,
        Boolean dmPermission,
        Boolean nsfw,
        Map<String, String> nameLocalizations,
        Map<String, String> descriptionLocalizations,
        List<Integer> contexts
) {
    public static final int CHAT_INPUT = 1;
    public static final int USER = 2;
    public static final int MESSAGE = 3;

    public SlashCommandDefinition(String name, String description, List<SlashCommandOptionDefinition> options) {
        this(CHAT_INPUT, name, description, options, null, null, null, Map.of(), Map.of(), List.of());
    }

    public SlashCommandDefinition {
        Objects.requireNonNull(name, "name");
        options = options == null ? List.of() : List.copyOf(options);
        nameLocalizations = nameLocalizations == null ? Map.of() : Map.copyOf(nameLocalizations);
        descriptionLocalizations = descriptionLocalizations == null ? Map.of() : Map.copyOf(descriptionLocalizations);
        contexts = contexts == null ? List.of() : List.copyOf(contexts);

        if (type < CHAT_INPUT || type > MESSAGE) {
            throw new IllegalArgumentException("Unsupported application command type: " + type);
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        if (type == CHAT_INPUT) {
            Objects.requireNonNull(description, "description");
            if (description.isBlank()) {
                throw new IllegalArgumentException("description must not be blank");
            }
        } else {
            if (!options.isEmpty()) {
                throw new IllegalArgumentException("Context menu commands do not support options");
            }
            if (description != null && !description.isBlank()) {
                throw new IllegalArgumentException("Context menu commands do not support description");
            }
            if (!descriptionLocalizations.isEmpty()) {
                throw new IllegalArgumentException("Context menu commands do not support description localizations");
            }
            description = null;
        }

        if (defaultMemberPermissions != null && !defaultMemberPermissions.isBlank()
                && !defaultMemberPermissions.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("defaultMemberPermissions must be a numeric string");
        }
    }

    public static SlashCommandDefinition simple(String name, String description) {
        return new SlashCommandDefinition(name, description, List.of());
    }

    public static SlashCommandDefinition userContextMenu(String name) {
        return new SlashCommandDefinition(USER, name, null, List.of(), null, null, null, Map.of(), Map.of(), List.of());
    }

    public static SlashCommandDefinition messageContextMenu(String name) {
        return new SlashCommandDefinition(MESSAGE, name, null, List.of(), null, null, null, Map.of(), Map.of(), List.of());
    }

    public SlashCommandDefinition withDefaultMemberPermissions(long permissions) {
        return withDefaultMemberPermissions(DiscordPermissions.asString(permissions));
    }

    public SlashCommandDefinition withDefaultMemberPermissions(String permissionsBitset) {
        return new SlashCommandDefinition(type, name, description, options, permissionsBitset, dmPermission, nsfw,
                nameLocalizations, descriptionLocalizations, contexts);
    }

    public SlashCommandDefinition withDmPermission(boolean isAllowedInDm) {
        return new SlashCommandDefinition(type, name, description, options, defaultMemberPermissions, isAllowedInDm, nsfw,
                nameLocalizations, descriptionLocalizations, contexts);
    }

    public SlashCommandDefinition withNsfw(boolean isNsfw) {
        return new SlashCommandDefinition(type, name, description, options, defaultMemberPermissions, dmPermission, isNsfw,
                nameLocalizations, descriptionLocalizations, contexts);
    }

    public SlashCommandDefinition withNameLocalizations(Map<String, String> localizations) {
        return new SlashCommandDefinition(type, name, description, options, defaultMemberPermissions, dmPermission, nsfw,
                localizations, descriptionLocalizations, contexts);
    }

    public SlashCommandDefinition withDescriptionLocalizations(Map<String, String> localizations) {
        return new SlashCommandDefinition(type, name, description, options, defaultMemberPermissions, dmPermission, nsfw,
                nameLocalizations, localizations, contexts);
    }

    public SlashCommandDefinition withContexts(List<Integer> allowedContexts) {
        return new SlashCommandDefinition(type, name, description, options, defaultMemberPermissions, dmPermission, nsfw,
                nameLocalizations, descriptionLocalizations, allowedContexts);
    }

    public Map<String, Object> toRequestPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", type);
        payload.put("name", name);

        if (!nameLocalizations.isEmpty()) {
            payload.put("name_localizations", nameLocalizations);
        }

        if (type == CHAT_INPUT) {
            payload.put("description", description);
            if (!descriptionLocalizations.isEmpty()) {
                payload.put("description_localizations", descriptionLocalizations);
            }
            if (!options.isEmpty()) {
                payload.put("options", options.stream().map(SlashCommandOptionDefinition::toRequestPayload).toList());
            }
        }

        if (defaultMemberPermissions != null && !defaultMemberPermissions.isBlank()) {
            payload.put("default_member_permissions", defaultMemberPermissions);
        }

        if (dmPermission != null) {
            payload.put("dm_permission", dmPermission);
        }

        if (nsfw != null) {
            payload.put("nsfw", nsfw);
        }

        if (!contexts.isEmpty()) {
            payload.put("contexts", contexts);
        }

        return payload;
    }
}
