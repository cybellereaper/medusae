package com.github.cybellereaper.commands.discord.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.cybellereaper.client.*;
import com.github.cybellereaper.commands.core.model.CommandInteraction;
import com.github.cybellereaper.commands.core.model.CommandOptionValue;
import com.github.cybellereaper.commands.core.model.CommandType;
import com.github.cybellereaper.commands.discord.adapter.payload.DiscordInteractionPayload;
import com.github.cybellereaper.commands.discord.adapter.payload.DiscordInteractionPayloadReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DiscordInteractionMapper {
    private static final com.fasterxml.jackson.databind.ObjectMapper JSON = new com.fasterxml.jackson.databind.ObjectMapper();
    private final DiscordInteractionPayloadReader payloadReader;

    public DiscordInteractionMapper() {
        this(new DiscordInteractionPayloadReader(new com.fasterxml.jackson.databind.ObjectMapper()));
    }

    DiscordInteractionMapper(DiscordInteractionPayloadReader payloadReader) {
        this.payloadReader = payloadReader;
    }

    public CommandInteraction toCoreInteraction(JsonNode interaction, InteractionContext context) {
        Objects.requireNonNull(interaction, "interaction");
        Objects.requireNonNull(context, "context");
        return toCoreInteraction(payloadReader.read(interaction), interaction, context);
    }

    CommandInteraction toCoreInteraction(DiscordInteractionPayload interactionPayload, JsonNode rawInteraction, InteractionContext context) {
        var data = interactionPayload.data();
        CommandType commandType = mapCommandType(orDefault(data == null ? null : data.type(), 1));

        ParsedData parsed = parseOptions(data == null ? null : data.options(), data == null ? null : data.resolved(), new ParsedData());

        String targetId = textOrNull(data == null ? null : data.targetId());
        ResolvedUser targetUser = commandType == CommandType.USER_CONTEXT && targetId != null ? context.resolvedUserValue(targetId) : null;
        ResolvedMember targetMember = commandType == CommandType.USER_CONTEXT && targetId != null ? context.resolvedMemberValue(targetId) : null;
        ResolvedMessage targetMessage = commandType == CommandType.MESSAGE_CONTEXT && targetId != null
                ? resolvedMessage(data == null ? null : data.resolved(), targetId)
                : null;

        return new CommandInteraction(
                data == null ? "" : stringOrEmpty(data.name()),
                commandType,
                parsed.group,
                parsed.subcommand,
                parsed.options,
                focusedOption(data == null ? null : data.options()),
                rawInteraction,
                context.guildId() == null,
                context.guildId(),
                context.userId(),
                Set.of(),
                Set.of(),
                targetUser,
                targetMember,
                null,
                null,
                null,
                targetMessage,
                parsed.optionUsers,
                parsed.optionMembers,
                parsed.optionChannels,
                parsed.optionRoles,
                parsed.optionAttachments
        );
    }

    private static CommandType mapCommandType(int discordType) {
        return switch (discordType) {
            case 2 -> CommandType.USER_CONTEXT;
            case 3 -> CommandType.MESSAGE_CONTEXT;
            default -> CommandType.CHAT_INPUT;
        };
    }

    private static String focusedOption(java.util.List<DiscordInteractionPayload.Option> options) {
        if (options == null) {
            return null;
        }
        for (DiscordInteractionPayload.Option option : options) {
            if (Boolean.TRUE.equals(option.focused())) {
                return textOrNull(option.name());
            }
            String nested = focusedOption(option.options());
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private static ParsedData parseOptions(java.util.List<DiscordInteractionPayload.Option> nodes,
                                           DiscordInteractionPayload.Resolved globalResolved,
                                           ParsedData parsedData) {
        if (nodes == null) {
            return parsedData;
        }

        for (DiscordInteractionPayload.Option option : nodes) {
            int type = orDefault(option.type(), 0);
            String name = textOrNull(option.name());
            if (name == null) {
                continue;
            }

            if (type == 1) {
                parsedData.subcommand = name;
                parseOptions(option.options(), globalResolved, parsedData);
                continue;
            }
            if (type == 2) {
                parsedData.group = name;
                var children = option.options();
                if (children != null && !children.isEmpty()) {
                    DiscordInteractionPayload.Option subcommandNode = children.get(0);
                    parsedData.subcommand = textOrNull(subcommandNode.name());
                    parseOptions(subcommandNode.options(), globalResolved, parsedData);
                }
                continue;
            }

            Object rawValue = option.value();
            parsedData.options.put(name, new CommandOptionValue(rawValue, type));
            var resolvedSource = option.resolved() == null ? globalResolved : option.resolved();
            parsedData.collectResolvedEntities(name, type, rawValue, resolvedSource);
        }

        return parsedData;
    }

    private static String textOrNull(String text) {
        return text == null || text.isBlank() ? null : text;
    }

    private static final class ParsedData {
        private String group;
        private String subcommand;
        private final Map<String, CommandOptionValue> options = new HashMap<>();
        private final Map<String, ResolvedUser> optionUsers = new HashMap<>();
        private final Map<String, ResolvedMember> optionMembers = new HashMap<>();
        private final Map<String, ResolvedChannel> optionChannels = new HashMap<>();
        private final Map<String, ResolvedRole> optionRoles = new HashMap<>();
        private final Map<String, ResolvedAttachment> optionAttachments = new HashMap<>();

        private void collectResolvedEntities(String optionName, int optionType, Object rawValue, DiscordInteractionPayload.Resolved resolved) {
            String entityId = resolveEntityId(rawValue);
            if (entityId == null) {
                return;
            }

            switch (optionType) {
                case 6 -> {
                    ResolvedUser user = resolvedUser(resolved, entityId);
                    ResolvedMember member = resolvedMember(resolved, entityId);
                    putIfPresent(optionUsers, optionName, user);
                    putIfPresent(optionMembers, optionName, member);
                }
                case 7 -> putIfPresent(optionChannels, optionName,
                        resolvedChannel(resolved, entityId));
                case 8 -> putIfPresent(optionRoles, optionName,
                        resolvedRole(resolved, entityId));
                case 11 -> putIfPresent(optionAttachments, optionName,
                        resolvedAttachment(resolved, entityId));
                default -> {
                    // not a resolved-entity option
                }
            }
        }


        private static <T> void putIfPresent(Map<String, T> map, String key, T value) {
            if (key != null && value != null) {
                map.put(key, value);
            }
        }

        private static String resolveEntityId(Object rawValue) {
            if (rawValue instanceof String value && !value.isBlank()) {
                return value;
            }
            if (rawValue instanceof Number number) {
                return Long.toString(number.longValue());
            }
            return null;
        }
    }


    public com.github.cybellereaper.commands.core.model.InteractionExecution toComponentInteraction(JsonNode interaction, InteractionContext context, com.github.cybellereaper.commands.core.model.InteractionHandlerType type) {
        Objects.requireNonNull(type, "type");
        DiscordInteractionPayload payload = payloadReader.read(interaction);
        return new com.github.cybellereaper.commands.core.model.InteractionExecution(
                type,
                textOrNull(payload.data() == null ? null : payload.data().customId()),
                Map.of(),
                interaction,
                context.guildId() == null,
                context.guildId(),
                context.userId(),
                Set.of(),
                Set.of(),
                extractStatePayload(textOrNull(payload.data() == null ? null : payload.data().customId()))
        );
    }

    public com.github.cybellereaper.commands.core.model.InteractionExecution toModalInteraction(JsonNode interaction, InteractionContext context) {
        Map<String, String> fields = new HashMap<>();
        DiscordInteractionPayload payload = payloadReader.read(interaction);
        var rows = payload.data() == null ? null : payload.data().components();
        if (rows != null) {
            for (DiscordInteractionPayload.ActionRow row : rows) {
                if (row.components() == null) continue;
                for (DiscordInteractionPayload.Component component : row.components()) {
                    String id = textOrNull(component.customId());
                    String value = textOrNull(component.value());
                    if (id != null && value != null) {
                        fields.put(id, value);
                    }
                }
            }
        }

        String customId = textOrNull(payload.data() == null ? null : payload.data().customId());
        return new com.github.cybellereaper.commands.core.model.InteractionExecution(
                com.github.cybellereaper.commands.core.model.InteractionHandlerType.MODAL,
                customId,
                fields,
                interaction,
                context.guildId() == null,
                context.guildId(),
                context.userId(),
                Set.of(),
                Set.of(),
                extractStatePayload(customId)
        );
    }

    private static String extractStatePayload(String customId) {
        if (customId == null) return null;
        int index = customId.indexOf('|');
        if (index < 0 || index + 1 >= customId.length()) {
            return null;
        }
        return customId.substring(index + 1);
    }

    Integer componentType(JsonNode interaction) {
        DiscordInteractionPayload payload = payloadReader.read(interaction);
        return payload.data() == null ? null : payload.data().componentType();
    }

    private static int orDefault(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static String stringOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static JsonNode mapNode(Map<String, Object> values, String id) {
        if (values == null || id == null) {
            return null;
        }
        Object value = values.get(id);
        if (value == null) {
            return null;
        }
        return JSON.valueToTree(value);
    }

    private static ResolvedMessage resolvedMessage(DiscordInteractionPayload.Resolved resolved, String targetId) {
        JsonNode node = mapNode(resolved == null ? null : resolved.messages(), targetId);
        return node == null ? null : ResolvedMessage.from(node);
    }

    private static ResolvedUser resolvedUser(DiscordInteractionPayload.Resolved resolved, String entityId) {
        JsonNode node = mapNode(resolved == null ? null : resolved.users(), entityId);
        return node == null ? null : ResolvedUser.from(node);
    }

    private static ResolvedMember resolvedMember(DiscordInteractionPayload.Resolved resolved, String entityId) {
        JsonNode node = mapNode(resolved == null ? null : resolved.members(), entityId);
        return node == null ? null : ResolvedMember.from(entityId, node);
    }

    private static ResolvedChannel resolvedChannel(DiscordInteractionPayload.Resolved resolved, String entityId) {
        JsonNode node = mapNode(resolved == null ? null : resolved.channels(), entityId);
        return node == null ? null : ResolvedChannel.from(node);
    }

    private static ResolvedRole resolvedRole(DiscordInteractionPayload.Resolved resolved, String entityId) {
        JsonNode node = mapNode(resolved == null ? null : resolved.roles(), entityId);
        return node == null ? null : ResolvedRole.from(node);
    }

    private static ResolvedAttachment resolvedAttachment(DiscordInteractionPayload.Resolved resolved, String entityId) {
        JsonNode node = mapNode(resolved == null ? null : resolved.attachments(), entityId);
        return node == null ? null : ResolvedAttachment.from(node);
    }
}
