package com.github.cybellereaper.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

final class SlashCommandRouter {
    private static final String DATA_FIELD = "data";
    private static final String ID_FIELD = "id";
    private static final String TOKEN_FIELD = "token";

    private final Map<String, Consumer<InteractionContext>> slashHandlers = new ConcurrentHashMap<>();
    private final Map<String, Consumer<InteractionContext>> userContextMenuHandlers = new ConcurrentHashMap<>();
    private final Map<String, Consumer<InteractionContext>> messageContextMenuHandlers = new ConcurrentHashMap<>();
    private final Map<String, Consumer<InteractionContext>> componentHandlers = new ConcurrentHashMap<>();
    private final Map<String, Consumer<InteractionContext>> modalHandlers = new ConcurrentHashMap<>();
    private final List<RouteHandler> componentTemplateHandlers = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<RouteHandler> modalTemplateHandlers = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final Map<String, Consumer<InteractionContext>> autocompleteHandlers = new ConcurrentHashMap<>();
    private final InteractionResponder responder;

    SlashCommandRouter(InteractionResponder responder) {
        this.responder = Objects.requireNonNull(responder, "responder");
    }

    void registerSlashHandler(String commandName, Consumer<JsonNode> handler) {
        Objects.requireNonNull(handler, "handler");
        registerSlashContextHandler(commandName, context -> handler.accept(context.raw()));
    }

    void registerComponentHandler(String customId, Consumer<JsonNode> handler) {
        Objects.requireNonNull(handler, "handler");
        registerComponentContextHandler(customId, context -> handler.accept(context.raw()));
    }

    void registerModalHandler(String customId, Consumer<JsonNode> handler) {
        Objects.requireNonNull(handler, "handler");
        registerModalContextHandler(customId, context -> handler.accept(context.raw()));
    }

    void registerAutocompleteHandler(String commandName, Consumer<JsonNode> handler) {
        Objects.requireNonNull(handler, "handler");
        registerAutocompleteContextHandler(commandName, context -> handler.accept(context.raw()));
    }

    void registerUserContextMenuHandler(String commandName, Consumer<JsonNode> handler) {
        Objects.requireNonNull(handler, "handler");
        registerUserContextMenuContextHandler(commandName, context -> handler.accept(context.raw()));
    }

    void registerMessageContextMenuHandler(String commandName, Consumer<JsonNode> handler) {
        Objects.requireNonNull(handler, "handler");
        registerMessageContextMenuContextHandler(commandName, context -> handler.accept(context.raw()));
    }

    void registerSlashContextHandler(String commandName, InteractionHandler handler) {
        registerUniqueHandler(slashHandlers, commandName, "slash command", handler::handle);
    }

    void registerComponentContextHandler(String customId, InteractionHandler handler) {
        registerRouteAwareHandler(componentHandlers, componentTemplateHandlers, customId, "component", handler::handle);
    }

    void registerModalContextHandler(String customId, InteractionHandler handler) {
        registerRouteAwareHandler(modalHandlers, modalTemplateHandlers, customId, "modal", handler::handle);
    }

    void registerAutocompleteContextHandler(String commandName, InteractionHandler handler) {
        registerUniqueHandler(autocompleteHandlers, commandName, "autocomplete", handler::handle);
    }

    void registerUserContextMenuContextHandler(String commandName, InteractionHandler handler) {
        registerUniqueHandler(userContextMenuHandlers, commandName, "user context menu", handler::handle);
    }

    void registerMessageContextMenuContextHandler(String commandName, InteractionHandler handler) {
        registerUniqueHandler(messageContextMenuHandlers, commandName, "message context menu", handler::handle);
    }

    void handleInteraction(JsonNode interaction) {
        if (interaction == null) {
            return;
        }

        InteractionType interactionType = InteractionType.fromCode(interaction.path("type").asInt());
        switch (interactionType) {
            case PING -> respond(interaction, ResponseType.PONG, null);
            case APPLICATION_COMMAND -> handleApplicationCommand(interaction);
            case APPLICATION_COMMAND_AUTOCOMPLETE -> dispatchByDataField(interaction, autocompleteHandlers, DataField.NAME);
            case MESSAGE_COMPONENT -> dispatchByDataField(interaction, componentHandlers, componentTemplateHandlers, DataField.CUSTOM_ID);
            case MODAL_SUBMIT -> dispatchByDataField(interaction, modalHandlers, modalTemplateHandlers, DataField.CUSTOM_ID);
            case UNKNOWN -> {
                // Unknown interaction types are intentionally ignored.
            }
        }
    }

    void respondWithMessage(JsonNode interaction, String content) {
        respondWithMessage(interaction, DiscordMessage.ofContent(content));
    }

    void respondWithMessage(JsonNode interaction, DiscordMessage message) {
        Objects.requireNonNull(message, "message");
        respond(interaction, ResponseType.CHANNEL_MESSAGE, message.toPayload());
    }

    void respondWithEmbeds(JsonNode interaction, String content, List<DiscordEmbed> embeds) {
        respondWithMessage(interaction, DiscordMessage.ofEmbeds(content, embeds));
    }

    void respondEphemeral(JsonNode interaction, String content) {
        respondWithMessage(interaction, DiscordMessage.ofContent(content).asEphemeral());
    }

    void respondEphemeralWithEmbeds(JsonNode interaction, String content, List<DiscordEmbed> embeds) {
        respondWithMessage(interaction, DiscordMessage.ofEmbeds(content, embeds).asEphemeral());
    }

    void respondWithModal(JsonNode interaction, DiscordModal modal) {
        Objects.requireNonNull(modal, "modal");
        respond(interaction, ResponseType.MODAL, modal.toPayload());
    }

    void respondWithAutocompleteChoices(JsonNode interaction, List<AutocompleteChoice> choices) {
        Objects.requireNonNull(choices, "choices");

        respond(interaction, ResponseType.AUTOCOMPLETE, Map.of(
                "choices", choices.stream().map(AutocompleteChoice::toPayload).toList()
        ));
    }

    void deferMessage(JsonNode interaction) {
        respond(interaction, ResponseType.DEFERRED_CHANNEL_MESSAGE, null);
    }

    void deferUpdate(JsonNode interaction) {
        respond(interaction, ResponseType.DEFERRED_MESSAGE_UPDATE, null);
    }

    String getOptionString(JsonNode interaction, String optionName) {
        return InteractionContext.from(interaction, responder).optionString(optionName);
    }

    String getModalValue(JsonNode interaction, String customId) {
        return InteractionContext.from(interaction, responder).modalValue(customId);
    }

    private void handleApplicationCommand(JsonNode interaction) {
        CommandType commandType = CommandType.fromCode(
                interaction.path(DATA_FIELD).path("type").asInt(CommandType.CHAT_INPUT.code())
        );

        Map<String, Consumer<InteractionContext>> handlers = switch (commandType) {
            case USER_CONTEXT -> userContextMenuHandlers;
            case MESSAGE_CONTEXT -> messageContextMenuHandlers;
            case CHAT_INPUT, UNKNOWN -> slashHandlers;
        };

        dispatchByDataField(interaction, handlers, DataField.NAME);
    }

    private void dispatchByDataField(JsonNode interaction, Map<String, Consumer<InteractionContext>> handlers, DataField dataField) {
        String handlerKey = interaction.path(DATA_FIELD).path(dataField.value()).asText("").trim();
        Consumer<InteractionContext> contextHandler = handlers.get(handlerKey);
        if (contextHandler != null) {
            contextHandler.accept(InteractionContext.from(interaction, responder));
        }
    }

    private void dispatchByDataField(
            JsonNode interaction,
            Map<String, Consumer<InteractionContext>> handlers,
            List<RouteHandler> routeHandlers,
            DataField dataField
    ) {
        String handlerKey = interaction.path(DATA_FIELD).path(dataField.value()).asText("").trim();
        Consumer<InteractionContext> exact = handlers.get(handlerKey);
        if (exact != null) {
            exact.accept(InteractionContext.from(interaction, responder));
            return;
        }
        for (RouteHandler routeHandler : routeHandlers) {
            if (routeHandler.matches(handlerKey)) {
                routeHandler.handler.accept(InteractionContext.from(interaction, responder));
                return;
            }
        }
    }

    private void respond(JsonNode interaction, ResponseType responseType, Map<String, Object> data) {
        Objects.requireNonNull(interaction, "interaction");

        String interactionId = interaction.path(ID_FIELD).asText();
        String interactionToken = interaction.path(TOKEN_FIELD).asText();

        if (interactionId.isBlank() || interactionToken.isBlank()) {
            throw new IllegalArgumentException("interaction must include id and token");
        }

        responder.respond(interactionId, interactionToken, responseType.code(), data);
    }

    private static void registerUniqueHandler(
            Map<String, Consumer<InteractionContext>> handlers,
            String key,
            String handlerType,
            Consumer<InteractionContext> handler
    ) {
        String normalizedKey = validateKey(key, handlerType);
        Objects.requireNonNull(handler, "handler");

        Consumer<InteractionContext> previous = handlers.putIfAbsent(normalizedKey, handler);
        if (previous != null) {
            throw new IllegalArgumentException("Interaction handler already registered for " + handlerType + ": " + normalizedKey);
        }
    }

    private static void registerRouteAwareHandler(
            Map<String, Consumer<InteractionContext>> handlers,
            List<RouteHandler> routeHandlers,
            String key,
            String handlerType,
            Consumer<InteractionContext> handler
    ) {
        String normalizedKey = validateKey(key, handlerType);
        if (normalizedKey.contains("{") && normalizedKey.contains("}")) {
            RouteHandler routeHandler = RouteHandler.compile(normalizedKey, handler);
            boolean conflict = routeHandlers.stream().anyMatch(existing -> existing.conflicts(routeHandler));
            if (conflict) {
                throw new IllegalArgumentException("Interaction handler already registered for " + handlerType + " route: " + normalizedKey);
            }
            routeHandlers.add(routeHandler);
            return;
        }
        registerUniqueHandler(handlers, normalizedKey, handlerType, handler);
    }

    private static final class RouteHandler {
        private final String template;
        private final String[] segments;
        private final boolean[] parameter;
        private final Consumer<InteractionContext> handler;

        private RouteHandler(String template, String[] segments, boolean[] parameter, Consumer<InteractionContext> handler) {
            this.template = template;
            this.segments = segments;
            this.parameter = parameter;
            this.handler = handler;
        }

        private static RouteHandler compile(String template, Consumer<InteractionContext> handler) {
            String[] segments = template.split(":");
            boolean[] parameter = new boolean[segments.length];
            for (int i = 0; i < segments.length; i++) {
                String current = segments[i];
                if (current.startsWith("{") && current.endsWith("}")) {
                    parameter[i] = true;
                }
            }
            return new RouteHandler(template, segments, parameter, handler);
        }

        private boolean matches(String customId) {
            String[] input = customId.split(":");
            if (input.length != segments.length) {
                return false;
            }
            for (int i = 0; i < input.length; i++) {
                if (!parameter[i] && !segments[i].equals(input[i])) {
                    return false;
                }
            }
            return true;
        }

        private boolean conflicts(RouteHandler other) {
            if (segments.length != other.segments.length) {
                return false;
            }
            for (int i = 0; i < segments.length; i++) {
                if (!parameter[i] && !other.parameter[i] && !segments[i].equals(other.segments[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    private static String validateKey(String key, String keyType) {
        Objects.requireNonNull(key, keyType + " key");
        String normalized = key.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(keyType + " key must not be blank");
        }
        return normalized;
    }

    private enum InteractionType {
        PING(1),
        APPLICATION_COMMAND(2),
        MESSAGE_COMPONENT(3),
        APPLICATION_COMMAND_AUTOCOMPLETE(4),
        MODAL_SUBMIT(5),
        UNKNOWN(-1);

        private static final Map<Integer, InteractionType> BY_CODE = byCode(values(), InteractionType::code);

        private final int code;

        InteractionType(int code) {
            this.code = code;
        }

        int code() {
            return code;
        }

        private static InteractionType fromCode(int code) {
            return BY_CODE.getOrDefault(code, UNKNOWN);
        }
    }

    private enum CommandType {
        CHAT_INPUT(1),
        USER_CONTEXT(2),
        MESSAGE_CONTEXT(3),
        UNKNOWN(-1);

        private static final Map<Integer, CommandType> BY_CODE = byCode(values(), CommandType::code);

        private final int code;

        CommandType(int code) {
            this.code = code;
        }

        int code() {
            return code;
        }

        private static CommandType fromCode(int code) {
            return BY_CODE.getOrDefault(code, UNKNOWN);
        }
    }

    private enum ResponseType {
        PONG(1),
        CHANNEL_MESSAGE(4),
        DEFERRED_CHANNEL_MESSAGE(5),
        DEFERRED_MESSAGE_UPDATE(6),
        AUTOCOMPLETE(8),
        MODAL(9);

        private final int code;

        ResponseType(int code) {
            this.code = code;
        }

        int code() {
            return code;
        }
    }

    private enum DataField {
        NAME("name"),
        CUSTOM_ID("custom_id");

        private final String value;

        DataField(String value) {
            this.value = value;
        }

        String value() {
            return value;
        }
    }

    private static <T> Map<Integer, T> byCode(T[] values, Function<T, Integer> keyMapper) {
        return Arrays.stream(values).collect(Collectors.toUnmodifiableMap(keyMapper, Function.identity()));
    }

    @FunctionalInterface
    interface InteractionResponder {
        void respond(String interactionId, String interactionToken, int type, Map<String, Object> data);
    }
}
