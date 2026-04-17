package com.github.cybellereaper.medusae.commands.discord.schema;

import com.github.cybellereaper.medusae.client.SlashCommandDefinition;
import com.github.cybellereaper.medusae.client.SlashCommandOptionDefinition;
import com.github.cybellereaper.medusae.commands.core.model.CommandDefinition;
import com.github.cybellereaper.medusae.commands.core.model.CommandHandler;
import com.github.cybellereaper.medusae.commands.core.model.CommandParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DiscordCommandSchemaExporter {

    public List<SlashCommandDefinition> export(Iterable<CommandDefinition> definitions) {
        List<SlashCommandDefinition> result = new ArrayList<>();
        for (CommandDefinition definition : definitions) {
            result.add(exportDefinition(definition));
        }
        return result;
    }

    public SlashCommandDefinition exportDefinition(CommandDefinition definition) {
        SlashCommandDefinition exported = switch (definition.type()) {
            case USER_CONTEXT -> SlashCommandDefinition.userContextMenu(definition.name());
            case MESSAGE_CONTEXT -> SlashCommandDefinition.messageContextMenu(definition.name());
            case CHAT_INPUT -> exportSlash(definition);
        };
        return applyMetadata(exported, definition);
    }

    private SlashCommandDefinition applyMetadata(SlashCommandDefinition command, CommandDefinition definition) {
        SlashCommandDefinition updated = command;
        if (definition.defaultMemberPermissions() != null) {
            updated = updated.withDefaultMemberPermissions(definition.defaultMemberPermissions());
        }
        if (definition.dmPermission() != null) {
            updated = updated.withDmPermission(definition.dmPermission());
        }
        if (definition.nsfw() != null) {
            updated = updated.withNsfw(definition.nsfw());
        }
        if (!definition.nameLocalizations().isEmpty()) {
            updated = updated.withNameLocalizations(definition.nameLocalizations());
        }
        if (!definition.descriptionLocalizations().isEmpty()) {
            updated = updated.withDescriptionLocalizations(definition.descriptionLocalizations());
        }
        if (!definition.contexts().isEmpty()) {
            updated = updated.withContexts(definition.contexts().stream().map(it -> it.apiValue()).toList());
        }
        return updated;
    }

    private SlashCommandDefinition exportSlash(CommandDefinition definition) {
        List<SlashCommandOptionDefinition> options = new ArrayList<>();
        List<CommandHandler> routedHandlers = definition.handlers().stream().filter(h -> h.subcommand() != null).toList();

        if (!routedHandlers.isEmpty()) {
            Map<String, List<CommandHandler>> grouped = routedHandlers.stream()
                    .filter(it -> it.subcommandGroup() != null)
                    .collect(Collectors.groupingBy(CommandHandler::subcommandGroup));
            grouped.forEach((groupName, handlers) -> options.add(SlashCommandOptionDefinition.subcommandGroup(
                    groupName,
                    "Subcommand group",
                    handlers.stream().map(this::toSubcommand).toList()
            )));
            routedHandlers.stream().filter(it -> it.subcommandGroup() == null).forEach(handler -> options.add(toSubcommand(handler)));
        }

        definition.handlers().stream().filter(h -> h.subcommand() == null).findFirst()
                .ifPresent(root -> root.parameters().stream().filter(this::isSchemaOption).map(this::toOption).forEach(options::add));

        return new SlashCommandDefinition(definition.name(), definition.description(), options);
    }

    private SlashCommandOptionDefinition toSubcommand(CommandHandler handler) {
        List<SlashCommandOptionDefinition> options = handler.parameters().stream().filter(this::isSchemaOption).map(this::toOption).toList();
        return SlashCommandOptionDefinition.subcommand(handler.subcommand(), handler.description(), options);
    }

    private boolean isSchemaOption(CommandParameter parameter) {
        return switch (parameter.kind()) {
            case OPTION, TARGET_USER, TARGET_MEMBER, TARGET_CHANNEL, TARGET_ROLE, TARGET_ATTACHMENT -> true;
            default -> false;
        };
    }

    private SlashCommandOptionDefinition toOption(CommandParameter parameter) {
        return new SlashCommandOptionDefinition(mapOptionType(parameter), parameter.optionName(), parameter.description(), parameter.required(), parameter.autocompleteId() != null);
    }

    private int mapOptionType(CommandParameter parameter) {
        Class<?> type = parameter.optionType();
        if (type == String.class || type.isEnum()) {
            return SlashCommandOptionDefinition.STRING;
        }
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
            return SlashCommandOptionDefinition.INTEGER;
        }
        if (type == boolean.class || type == Boolean.class) {
            return SlashCommandOptionDefinition.BOOLEAN;
        }
        if (type == double.class || type == Double.class) {
            return SlashCommandOptionDefinition.NUMBER;
        }
        return switch (parameter.kind()) {
            case TARGET_USER, TARGET_MEMBER -> SlashCommandOptionDefinition.USER;
            case TARGET_CHANNEL -> SlashCommandOptionDefinition.CHANNEL;
            case TARGET_ROLE -> SlashCommandOptionDefinition.ROLE;
            case TARGET_ATTACHMENT -> SlashCommandOptionDefinition.ATTACHMENT;
            default -> SlashCommandOptionDefinition.STRING;
        };
    }
}
