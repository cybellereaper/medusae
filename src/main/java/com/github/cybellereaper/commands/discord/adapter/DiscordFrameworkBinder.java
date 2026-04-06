package com.github.cybellereaper.commands.discord.adapter;

import com.github.cybellereaper.client.DiscordClient;
import com.github.cybellereaper.commands.core.execute.CommandFramework;
import com.github.cybellereaper.commands.core.interaction.UiHandlerType;
import com.github.cybellereaper.commands.core.model.CommandDefinition;
import com.github.cybellereaper.commands.core.model.CommandType;

import java.util.List;
import java.util.Objects;

public final class DiscordFrameworkBinder {
    private DiscordFrameworkBinder() {
    }

    public static List<CommandBinding> planBindings(CommandFramework framework) {
        Objects.requireNonNull(framework, "framework");
        return framework.registry().all().stream()
                .map(definition -> new CommandBinding(
                        definition.name(),
                        definition.type(),
                        requiresAutocompleteRegistration(definition)
                ))
                .toList();
    }

    public static void bind(DiscordClient client, CommandFramework framework, DiscordCommandDispatcher dispatcher) {
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(framework, "framework");
        Objects.requireNonNull(dispatcher, "dispatcher");

        framework.registry().all().forEach(definition -> registerDefinition(client, dispatcher, definition));
        framework.uiRegistry().all().forEach(handler -> registerUiHandler(client, dispatcher, handler.type(), handler.route().template()));
    }

    static boolean requiresAutocompleteRegistration(CommandDefinition definition) {
        return !definition.autocompleteHandlers().isEmpty()
                || definition.handlers().stream()
                .anyMatch(handler -> handler.parameters().stream().anyMatch(parameter -> parameter.autocompleteId() != null));
    }

    private static void registerDefinition(DiscordClient client, DiscordCommandDispatcher dispatcher, CommandDefinition definition) {
        switch (definition.type()) {
            case CHAT_INPUT -> {
                client.onSlashCommandContext(definition.name(), context -> dispatcher.dispatch(context.raw(), context));
                if (requiresAutocompleteRegistration(definition)) {
                    client.onAutocompleteContext(definition.name(), context -> dispatcher.dispatchAutocomplete(context.raw(), context));
                }
            }
            case USER_CONTEXT -> client.onUserContextMenuContext(definition.name(), context -> dispatcher.dispatch(context.raw(), context));
            case MESSAGE_CONTEXT -> client.onMessageContextMenuContext(definition.name(), context -> dispatcher.dispatch(context.raw(), context));
        }
    }

    private static void registerUiHandler(DiscordClient client, DiscordCommandDispatcher dispatcher, UiHandlerType type, String routeTemplate) {
        switch (type) {
            case MODAL -> client.onModalSubmitContext(routeTemplate, context -> dispatcher.dispatchComponent(UiHandlerType.MODAL, context.raw(), context));
            case BUTTON, STRING_SELECT, USER_SELECT, ROLE_SELECT, MENTIONABLE_SELECT, CHANNEL_SELECT ->
                    client.onComponentInteractionContext(routeTemplate, context -> dispatcher.dispatchComponent(type, context.raw(), context));
        }
    }

    public record CommandBinding(String commandName, CommandType commandType, boolean hasAutocomplete) {
    }
}
