package com.github.cybellereaper.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Command definition + handlers that can be auto-registered and synced in one step.
 */
public final class CommandRegistration {
    private final SlashCommandDefinition definition;
    private final Consumer<JsonNode> slashHandler;
    private final SlashCommandHandler slashContextHandler;
    private final Consumer<JsonNode> autocompleteHandler;
    private final SlashCommandHandler autocompleteContextHandler;
    private final Consumer<JsonNode> userContextMenuHandler;
    private final InteractionHandler userContextMenuContextHandler;
    private final Consumer<JsonNode> messageContextMenuHandler;
    private final InteractionHandler messageContextMenuContextHandler;

    private CommandRegistration(Builder builder) {
        this.definition = builder.definition;
        this.slashHandler = builder.slashHandler;
        this.slashContextHandler = builder.slashContextHandler;
        this.autocompleteHandler = builder.autocompleteHandler;
        this.autocompleteContextHandler = builder.autocompleteContextHandler;
        this.userContextMenuHandler = builder.userContextMenuHandler;
        this.userContextMenuContextHandler = builder.userContextMenuContextHandler;
        this.messageContextMenuHandler = builder.messageContextMenuHandler;
        this.messageContextMenuContextHandler = builder.messageContextMenuContextHandler;
    }

    public static Builder builder(SlashCommandDefinition definition) {
        return new Builder(definition);
    }

    public SlashCommandDefinition definition() {
        return definition;
    }

    public String name() {
        return definition.name();
    }

    void registerHandlers(DiscordClient client) {
        Objects.requireNonNull(client, "client");

        switch (definition.type()) {
            case SlashCommandDefinition.CHAT_INPUT -> registerSlashHandlers(client);
            case SlashCommandDefinition.USER -> registerUserContextHandlers(client);
            case SlashCommandDefinition.MESSAGE -> registerMessageContextHandlers(client);
            default -> throw new IllegalArgumentException("Unsupported command type: " + definition.type());
        }
    }

    private void registerSlashHandlers(DiscordClient client) {
        if (slashHandler != null) {
            client.onSlashCommand(name(), slashHandler);
        }
        if (slashContextHandler != null) {
            client.onSlashCommandContext(name(), slashContextHandler);
        }
        if (autocompleteHandler != null) {
            client.onAutocomplete(name(), autocompleteHandler);
        }
        if (autocompleteContextHandler != null) {
            client.onAutocompleteContext(name(), autocompleteContextHandler);
        }
    }

    private void registerUserContextHandlers(DiscordClient client) {
        if (userContextMenuHandler != null) {
            client.onUserContextMenu(name(), userContextMenuHandler);
        }
        if (userContextMenuContextHandler != null) {
            client.onUserContextMenuContext(name(), userContextMenuContextHandler);
        }
    }

    private void registerMessageContextHandlers(DiscordClient client) {
        if (messageContextMenuHandler != null) {
            client.onMessageContextMenu(name(), messageContextMenuHandler);
        }
        if (messageContextMenuContextHandler != null) {
            client.onMessageContextMenuContext(name(), messageContextMenuContextHandler);
        }
    }

    public static final class Builder {
        private final SlashCommandDefinition definition;
        private Consumer<JsonNode> slashHandler;
        private SlashCommandHandler slashContextHandler;
        private Consumer<JsonNode> autocompleteHandler;
        private SlashCommandHandler autocompleteContextHandler;
        private Consumer<JsonNode> userContextMenuHandler;
        private InteractionHandler userContextMenuContextHandler;
        private Consumer<JsonNode> messageContextMenuHandler;
        private InteractionHandler messageContextMenuContextHandler;

        private Builder(SlashCommandDefinition definition) {
            this.definition = Objects.requireNonNull(definition, "definition");
        }

        public Builder onSlash(Consumer<JsonNode> handler) {
            this.slashHandler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public Builder onSlashContext(SlashCommandHandler handler) {
            this.slashContextHandler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public Builder onAutocomplete(Consumer<JsonNode> handler) {
            this.autocompleteHandler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public Builder onAutocompleteContext(SlashCommandHandler handler) {
            this.autocompleteContextHandler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public Builder onUserContextMenu(Consumer<JsonNode> handler) {
            this.userContextMenuHandler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public Builder onUserContextMenuContext(InteractionHandler handler) {
            this.userContextMenuContextHandler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public Builder onMessageContextMenu(Consumer<JsonNode> handler) {
            this.messageContextMenuHandler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public Builder onMessageContextMenuContext(InteractionHandler handler) {
            this.messageContextMenuContextHandler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public CommandRegistration build() {
            validateHandlerTypeCompatibility();
            return new CommandRegistration(this);
        }

        private void validateHandlerTypeCompatibility() {
            int commandType = definition.type();
            if (commandType == SlashCommandDefinition.CHAT_INPUT) {
                ensureNoContextMenuHandlers();
                return;
            }

            if (commandType == SlashCommandDefinition.USER) {
                ensureNoSlashHandlers();
                if (messageContextMenuHandler != null || messageContextMenuContextHandler != null) {
                    throw new IllegalStateException("User context menu commands cannot declare message context handlers");
                }
                return;
            }

            if (commandType == SlashCommandDefinition.MESSAGE) {
                ensureNoSlashHandlers();
                if (userContextMenuHandler != null || userContextMenuContextHandler != null) {
                    throw new IllegalStateException("Message context menu commands cannot declare user context handlers");
                }
                return;
            }

            throw new IllegalArgumentException("Unsupported command type: " + commandType);
        }

        private void ensureNoSlashHandlers() {
            if (slashHandler != null || slashContextHandler != null || autocompleteHandler != null || autocompleteContextHandler != null) {
                throw new IllegalStateException("Context menu commands cannot declare slash/autocomplete handlers");
            }
        }

        private void ensureNoContextMenuHandlers() {
            if (userContextMenuHandler != null || userContextMenuContextHandler != null
                    || messageContextMenuHandler != null || messageContextMenuContextHandler != null) {
                throw new IllegalStateException("Slash commands cannot declare context menu handlers");
            }
        }
    }
}
