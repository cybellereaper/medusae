package com.github.cybellereaper.commands.core.execute;

import com.github.cybellereaper.commands.core.autocomplete.AutocompleteRegistry;
import com.github.cybellereaper.commands.core.check.CheckRegistry;
import com.github.cybellereaper.commands.core.exception.CheckFailedException;
import com.github.cybellereaper.commands.core.exception.CommandNotFoundException;
import com.github.cybellereaper.commands.core.exception.RegistrationException;
import com.github.cybellereaper.commands.core.exception.ResolutionException;
import com.github.cybellereaper.commands.core.model.*;
import com.github.cybellereaper.commands.core.interaction.UiHandler;
import com.github.cybellereaper.commands.core.interaction.UiHandlerRegistry;
import com.github.cybellereaper.commands.core.interaction.UiParameter;
import com.github.cybellereaper.commands.core.interaction.UiHandlerType;
import com.github.cybellereaper.commands.core.parser.CommandParser;
import com.github.cybellereaper.commands.core.parser.UiHandlerParser;
import com.github.cybellereaper.commands.core.registry.CommandRegistry;
import com.github.cybellereaper.commands.core.resolve.ParameterResolver;
import com.github.cybellereaper.commands.core.resolve.ResolverRegistry;
import com.github.cybellereaper.commands.core.response.CommandResponse;
import com.github.cybellereaper.commands.core.response.ImmediateResponse;
import com.github.cybellereaper.commands.discord.context.ComponentContext;
import com.github.cybellereaper.commands.discord.context.InteractionContextBase;
import com.github.cybellereaper.commands.discord.context.ModalContext;
import com.github.cybellereaper.client.InteractionContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class CommandFramework {
    private final CommandRegistry commandRegistry = new CommandRegistry();
    private final ResolverRegistry resolverRegistry = new ResolverRegistry();
    private final CheckRegistry checkRegistry = new CheckRegistry();
    private final AutocompleteRegistry autocompleteRegistry = new AutocompleteRegistry();
    private final CooldownManager cooldownManager = new CooldownManager();
    private final CommandParser commandParser = new CommandParser();
    private final UiHandlerParser uiHandlerParser = new UiHandlerParser();
    private final UiHandlerRegistry uiHandlerRegistry = new UiHandlerRegistry();
    private CommandExceptionHandler exceptionHandler = CommandExceptionHandler.rethrowing();

    public void registerCommands(Object... handlers) {
        for (Object handler : handlers) {
            if (handler.getClass().isAnnotationPresent(com.github.cybellereaper.commands.core.annotation.Command.class)) {
                CommandDefinition parsed = commandParser.parse(handler);
                validateChecks(parsed);
                commandRegistry.register(parsed);
            }
            uiHandlerParser.parse(handler).forEach(uiHandlerRegistry::register);
        }
    }

    public <T> void registerResolver(Class<T> type, ParameterResolver<? extends T> resolver) {
        resolverRegistry.register(type, resolver);
    }

    public void registerCheck(String id, com.github.cybellereaper.commands.core.check.CommandCheck check) {
        checkRegistry.register(id, check);
    }

    public void registerAutocomplete(String id, com.github.cybellereaper.commands.core.autocomplete.AutocompleteProvider provider) {
        autocompleteRegistry.register(id, provider);
    }

    public void setExceptionHandler(CommandExceptionHandler exceptionHandler) {
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
    }

    public List<String> executeAutocomplete(CommandInteraction interaction, CommandResponder responder) {
        Objects.requireNonNull(interaction, "interaction");
        Objects.requireNonNull(responder, "responder");
        CommandDefinition definition = commandRegistry.find(interaction.commandName())
                .orElseThrow(() -> new CommandNotFoundException("Unknown command: " + interaction.commandName()));

        String option = interaction.focusedOption();
        if (option == null) {
            return List.of();
        }

        CommandContext context = new CommandContext(interaction, responder);
        for (AutocompleteHandler handler : definition.autocompleteHandlers()) {
            if (handler.optionName().equalsIgnoreCase(option)) {
                try {
                    @SuppressWarnings("unchecked")
                    List<String> values = (List<String>) handler.method().invoke(handler.instance(), context);
                    return values == null ? List.of() : values;
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    exceptionHandler.onException(context, unwrap(exception));
                    return List.of();
                }
            }
        }

        return definition.handlers().stream()
                .flatMap(h -> h.parameters().stream())
                .filter(p -> p.autocompleteId() != null && p.optionName().equalsIgnoreCase(option))
                .findFirst()
                .flatMap(p -> autocompleteRegistry.find(p.autocompleteId()))
                .map(provider -> provider.complete(context, optionValue(interaction, option)))
                .orElse(List.of());
    }

    public void execute(CommandInteraction interaction, CommandResponder responder) {
        Objects.requireNonNull(interaction, "interaction");
        Objects.requireNonNull(responder, "responder");
        CommandContext context = new CommandContext(interaction, responder);
        try {
            CommandDefinition definition = commandRegistry.find(interaction.commandName())
                    .orElseThrow(() -> new CommandNotFoundException("Unknown command: " + interaction.commandName()));
            CommandHandler handler = commandRegistry.findHandler(interaction.commandName(), interaction.routeKey())
                    .orElseThrow(() -> new CommandNotFoundException("Unknown subcommand route: " + interaction.routeKey()));

            enforceGuards(context, definition, handler);
            cooldownManager.enforce(definition.name() + ":" + (handler.routeKey() == null ? "root" : handler.routeKey()),
                    handler.cooldown() == null ? definition.cooldown() : handler.cooldown(), interaction);

            Object[] args = resolveParameters(context, handler);
            Object result = handler.method().invoke(handler.instance(), args);
            if (result instanceof CommandResponse response) {
                responder.accept(response);
            } else if (result instanceof String content) {
                responder.accept(ImmediateResponse.publicMessage(content));
            }
        } catch (Throwable throwable) {
            exceptionHandler.onException(context, unwrap(throwable));
        }
    }

    public void executeComponent(UiHandlerType type, String customId, JsonNode rawInteraction, InteractionContext interactionContext, CommandResponder responder) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(customId, "customId");
        Objects.requireNonNull(interactionContext, "interactionContext");
        Objects.requireNonNull(responder, "responder");

        UiHandlerRegistry.ResolvedUiHandler resolved = uiHandlerRegistry.resolve(type, customId)
                .orElseThrow(() -> new CommandNotFoundException("Unknown component route: " + customId));

        CommandInteraction guardInteraction = new CommandInteraction(
                interactionContext.commandName(),
                CommandType.CHAT_INPUT,
                null,
                null,
                Map.of(),
                null,
                rawInteraction,
                interactionContext.guildId() == null,
                interactionContext.guildId(),
                interactionContext.userId(),
                Set.of(),
                Set.of(),
                null, null, null, null, null, null
        );
        CommandContext checkContext = new CommandContext(guardInteraction, responder);
        enforceUiGuards(checkContext, resolved.handler());

        InteractionContextBase context = resolved.handler().type() == UiHandlerType.MODAL
                ? new ModalContext(interactionContext, rawInteraction, resolved.pathParams())
                : new ComponentContext(interactionContext, rawInteraction, resolved.pathParams());

        Object[] args = resolveUiParameters(context, resolved.handler(), rawInteraction);
        try {
            Object result = resolved.handler().method().invoke(resolved.handler().instance(), args);
            if (result instanceof CommandResponse response) {
                responder.accept(response);
            } else if (result instanceof String content) {
                responder.accept(ImmediateResponse.publicMessage(content));
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new ResolutionException("Failed to invoke UI handler: " + resolved.handler().method(), unwrap(exception));
        }
    }

    public CommandRegistry registry() {
        return commandRegistry;
    }

    public UiHandlerRegistry uiRegistry() {
        return uiHandlerRegistry;
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof InvocationTargetException invocationTargetException && invocationTargetException.getCause() != null) {
            return invocationTargetException.getCause();
        }
        return throwable;
    }

    private void enforceGuards(CommandContext context, CommandDefinition definition, CommandHandler handler) {
        if (definition.guildOnly() && context.interaction().dm()) {
            throw new CheckFailedException("Command is guild-only");
        }
        if (definition.dmOnly() && !context.interaction().dm()) {
            throw new CheckFailedException("Command is DM-only");
        }

        List<String> userPermissions = new ArrayList<>(definition.requiredUserPermissions());
        userPermissions.addAll(handler.requiredUserPermissions());
        if (!context.interaction().userPermissions().containsAll(userPermissions)) {
            throw new CheckFailedException("Missing user permissions: " + userPermissions);
        }

        List<String> botPermissions = new ArrayList<>(definition.requiredBotPermissions());
        botPermissions.addAll(handler.requiredBotPermissions());
        if (!context.interaction().botPermissions().containsAll(botPermissions)) {
            throw new CheckFailedException("Missing bot permissions: " + botPermissions);
        }

        List<String> checks = new ArrayList<>(definition.checks());
        checks.addAll(handler.checks());
        for (String checkId : checks) {
            boolean result = checkRegistry.find(checkId)
                    .orElseThrow(() -> new RegistrationException("Unknown check id '" + checkId + "'"))
                    .test(context);
            if (!result) {
                throw new CheckFailedException("Check failed: " + checkId);
            }
        }
    }

    private Object[] resolveParameters(CommandContext context, CommandHandler handler) {
        Object[] args = new Object[handler.parameters().size()];
        for (CommandParameter parameter : handler.parameters()) {
            args[parameter.index()] = resolveParameter(context, parameter);
        }
        return args;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object resolveParameter(CommandContext context, CommandParameter parameter) {
        Class<?> type = parameter.optionType();
        CommandInteraction interaction = context.interaction();

        return switch (parameter.kind()) {
            case CONTEXT -> context;
            case RAW_INTERACTION -> interaction.rawInteraction();
            case TARGET_USER -> resolveEntityParameter(interaction.targetUser(), interaction.optionUsers(), parameter.optionName());
            case TARGET_MEMBER -> resolveEntityParameter(interaction.targetMember(), interaction.optionMembers(), parameter.optionName());
            case TARGET_CHANNEL -> resolveEntityParameter(interaction.targetChannel(), interaction.optionChannels(), parameter.optionName());
            case TARGET_ROLE -> resolveEntityParameter(interaction.targetRole(), interaction.optionRoles(), parameter.optionName());
            case TARGET_ATTACHMENT -> resolveEntityParameter(interaction.targetAttachment(), interaction.optionAttachments(), parameter.optionName());
            case TARGET_MESSAGE -> interaction.targetMessage();
            case OPTION -> resolveOption(type, interaction, parameter);
            case CUSTOM -> resolverRegistry.find(type)
                    .map(resolver -> ((ParameterResolver) resolver).resolve(context, parameter))
                    .orElseThrow(() -> new ResolutionException("No resolver registered for custom type: " + type.getName()));
        };
    }

    private void enforceUiGuards(CommandContext context, UiHandler handler) {
        if (!context.interaction().userPermissions().containsAll(handler.requiredUserPermissions())) {
            throw new CheckFailedException("Missing user permissions: " + handler.requiredUserPermissions());
        }
        for (String checkId : handler.checks()) {
            boolean result = checkRegistry.find(checkId)
                    .orElseThrow(() -> new RegistrationException("Unknown check id '" + checkId + "'"))
                    .test(context);
            if (!result) {
                throw new CheckFailedException("Check failed: " + checkId);
            }
        }
    }

    private Object[] resolveUiParameters(InteractionContextBase context, UiHandler handler, JsonNode rawInteraction) {
        Object[] args = new Object[handler.parameters().size()];
        for (UiParameter parameter : handler.parameters()) {
            args[parameter.index()] = switch (parameter.kind()) {
                case COMPONENT_CONTEXT -> context instanceof ComponentContext cc ? cc : context;
                case MODAL_CONTEXT -> context instanceof ModalContext mc ? mc : context;
                case BASE_CONTEXT -> context;
                case RAW_INTERACTION -> rawInteraction;
                case PATH_PARAM -> convertString(context.pathParams().get(parameter.bindingKey()), parameter.targetType(), null);
                case FIELD -> context instanceof ModalContext modalContext
                        ? convertString(modalContext.fieldValue(parameter.bindingKey()), parameter.targetType(), null)
                        : null;
                case CUSTOM -> throw new ResolutionException("No resolver available for custom UI parameter: " + parameter.targetType().getName());
            };
        }
        return args;
    }

    private Object resolveOption(Class<?> type, CommandInteraction interaction, CommandParameter parameter) {
        CommandOptionValue option = interaction.options().get(parameter.optionName());
        if (option == null || option.value() == null) {
            if (parameter.defaultValue() != null) {
                Object fallbackValue = convertString(parameter.defaultValue(), type, parameter);
                return parameter.wrappedOptional() ? Optional.of(fallbackValue) : fallbackValue;
            }
            if (!parameter.required()) {
                if (parameter.wrappedOptional()) {
                    return Optional.empty();
                }
                if (type.isPrimitive()) {
                    return primitiveDefault(type);
                }
                return null;
            }
            throw new ResolutionException("Missing required option: " + parameter.optionName());
        }

        Object rawValue = option.value();
        Object resolvedValue;
        if (type.isInstance(rawValue)) {
            resolvedValue = rawValue;
            return parameter.wrappedOptional() ? Optional.of(resolvedValue) : resolvedValue;
        }
        if (rawValue instanceof String textValue) {
            resolvedValue = convertString(textValue, type, parameter);
            return parameter.wrappedOptional() ? Optional.of(resolvedValue) : resolvedValue;
        }
        if (rawValue instanceof Number number) {
            if (type == int.class || type == Integer.class) {
                resolvedValue = number.intValue();
                return parameter.wrappedOptional() ? Optional.of(resolvedValue) : resolvedValue;
            }
            if (type == long.class || type == Long.class) {
                resolvedValue = number.longValue();
                return parameter.wrappedOptional() ? Optional.of(resolvedValue) : resolvedValue;
            }
            if (type == double.class || type == Double.class) {
                resolvedValue = number.doubleValue();
                return parameter.wrappedOptional() ? Optional.of(resolvedValue) : resolvedValue;
            }
        }
        if (rawValue instanceof Boolean booleanValue && (type == boolean.class || type == Boolean.class)) {
            resolvedValue = booleanValue;
            return parameter.wrappedOptional() ? Optional.of(resolvedValue) : resolvedValue;
        }
        throw new ResolutionException("Invalid option type for '" + parameter.optionName() + "'. Expected " + type.getSimpleName());
    }


    private static Object resolveEntityParameter(Object contextTarget, java.util.Map<String, Object> optionTargets, String optionName) {
        if (contextTarget != null) {
            return contextTarget;
        }
        if (optionName == null) {
            return null;
        }
        return optionTargets.get(optionName);
    }
    private static Object convertString(String value, Class<?> type, CommandParameter parameter) {
        if (value == null) {
            if (type.isPrimitive()) {
                return primitiveDefault(type);
            }
            return null;
        }
        try {
            if (type == String.class) {
                return value;
            }
            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(value);
            }
            if (type == long.class || type == Long.class) {
                return Long.parseLong(value);
            }
            if (type == double.class || type == Double.class) {
                return Double.parseDouble(value);
            }
            if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(value);
            }
            if (type.isEnum()) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Object enumValue = Enum.valueOf((Class<? extends Enum>) type, value.toUpperCase());
                return enumValue;
            }
        } catch (Exception exception) {
            String optionName = parameter == null ? "<interaction>" : parameter.optionName();
            throw new ResolutionException("Failed to convert option '" + optionName + "' value: " + value, exception);
        }
        throw new ResolutionException("Unsupported option type: " + type.getName());
    }

    private static Object primitiveDefault(Class<?> primitiveType) {
        if (primitiveType == boolean.class) {
            return false;
        }
        if (primitiveType == int.class) {
            return 0;
        }
        if (primitiveType == long.class) {
            return 0L;
        }
        if (primitiveType == double.class) {
            return 0D;
        }
        return null;
    }

    private void validateChecks(CommandDefinition definition) {
        List<String> checks = new ArrayList<>(definition.checks());
        definition.handlers().forEach(handler -> checks.addAll(handler.checks()));
        for (String check : checks) {
            if (checkRegistry.find(check).isEmpty()) {
                throw new RegistrationException("Check '" + check + "' was referenced but is not registered");
            }
        }
    }

    private static String optionValue(CommandInteraction interaction, String option) {
        CommandOptionValue value = interaction.options().get(option);
        return value == null || value.value() == null ? "" : String.valueOf(value.value());
    }
}
