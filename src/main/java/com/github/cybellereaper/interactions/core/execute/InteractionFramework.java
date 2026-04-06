package com.github.cybellereaper.interactions.core.execute;

import com.github.cybellereaper.interactions.core.check.CheckRegistry;
import com.github.cybellereaper.interactions.core.context.*;
import com.github.cybellereaper.interactions.core.exception.*;
import com.github.cybellereaper.interactions.core.model.*;
import com.github.cybellereaper.interactions.core.response.InteractionResponse;
import com.github.cybellereaper.interactions.core.route.*;
import com.github.cybellereaper.interactions.core.session.SessionRegistry;
import com.github.cybellereaper.interactions.core.state.StateCodecRegistry;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class InteractionFramework {
    private final InteractionParser parser = new InteractionParser();
    private final InteractionRouteRegistry routeRegistry = new InteractionRouteRegistry();
    private final CheckRegistry checks = new CheckRegistry();
    private final StateCodecRegistry stateCodecs = new StateCodecRegistry();
    private final SessionRegistry sessions = new SessionRegistry();
    private final InteractionCooldowns cooldowns = new InteractionCooldowns();
    private InteractionExceptionHandler exceptionHandler = InteractionExceptionHandler.rethrowing();

    public void registerInteractions(Object... handlers) {
        for (Object handler : handlers) {
            for (InteractionHandlerDefinition definition : parser.parse(handler)) {
                routeRegistry.register(definition);
            }
        }
    }

    public void registerCondition(String id, com.github.cybellereaper.interactions.core.check.InteractionCheck check) {
        checks.register(id, check);
    }

    public <T> void registerStateCodec(Class<T> type, com.github.cybellereaper.interactions.core.state.StateCodec<T> codec) {
        stateCodecs.register(type, codec);
    }

    public void setExceptionHandler(InteractionExceptionHandler exceptionHandler) {
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
    }

    public InteractionResponse dispatch(InteractionPayload payload) {
        InteractionContext context = baseContext(payload);
        try {
            ResolvedInteractionRoute resolved = routeRegistry.resolve(payload.type(), payload.customId())
                    .orElseThrow(() -> new UnknownInteractionRouteException("Unknown route for type " + payload.type() + " and id '" + payload.customId() + "'"));

            sessions.findMatching(payload, resolved.definition().route().template());
            enforceContext(resolved.definition(), context);
            enforceChecks(resolved.definition(), context);
            cooldowns.enforce(resolved.definition(), payload);

            Object[] args = bindParameters(resolved, context, payload);
            Object result = resolved.definition().method().invoke(resolved.definition().instance(), args);
            if (result instanceof InteractionResponse response) {
                return response;
            }
            return context.immediateResponse() == null ? InteractionResponse.none() : context.immediateResponse();
        } catch (Throwable throwable) {
            exceptionHandler.onException(context, unwrap(throwable));
            return InteractionResponse.none();
        }
    }

    private void enforceChecks(InteractionHandlerDefinition definition, InteractionContext context) {
        for (String checkId : definition.checks()) {
            boolean result = checks.find(checkId)
                    .orElseThrow(() -> new RouteRegistrationException("Unknown interaction check '" + checkId + "'"))
                    .test(context);
            if (!result) {
                throw new InteractionCheckException("Interaction check failed: " + checkId);
            }
        }
    }

    private static void enforceContext(InteractionHandlerDefinition definition, InteractionContext context) {
        if (definition.guildOnly() && context.isDm()) {
            throw new InteractionCheckException("Handler is guild-only");
        }
        if (definition.dmOnly() && !context.isDm()) {
            throw new InteractionCheckException("Handler is DM-only");
        }
        if (definition.deferReply()) {
            context.deferReply(definition.ephemeralDefault());
        }
        if (definition.deferUpdate()) {
            context.deferUpdate();
        }
    }

    private Object[] bindParameters(ResolvedInteractionRoute resolved, InteractionContext context, InteractionPayload payload) {
        List<InteractionParameter> parameters = resolved.definition().parameters();
        Object[] args = new Object[parameters.size()];
        Map<String, String> params = resolved.match().pathParams();
        for (InteractionParameter parameter : parameters) {
            args[parameter.index()] = switch (parameter.kind()) {
                case CONTEXT -> castContext(context, parameter.type());
                case SELECTED_VALUES -> payload.selectedValues();
                case MODAL_FIELD -> payload.modalField(parameter.key())
                        .orElseThrow(() -> new InteractionBindingException("Missing modal field '" + parameter.key() + "'"));
                case PATH_PARAM -> convertScalar(params.get(parameter.key()), parameter.type(), parameter.key());
                case USER_ID -> payload.userId();
                case GUILD_ID -> payload.guildId();
                case CHANNEL_ID -> payload.channelId();
                case MESSAGE_ID -> payload.messageId();
                case RAW -> payload.rawInteraction();
                case CUSTOM_STATE -> throw new InteractionBindingException("Custom state bindings are not yet auto-bound; use @PathParam and codec manually.");
            };
        }
        return args;
    }

    private static Object castContext(InteractionContext context, Class<?> type) {
        if (type.isInstance(context)) {
            return context;
        }
        if (type == InteractionContext.class) {
            return context;
        }
        throw new InteractionBindingException("Unsupported context type: " + type.getName());
    }

    private static Object convertScalar(String raw, Class<?> type, String key) {
        if (raw == null) {
            throw new InteractionBindingException("Missing path parameter '" + key + "'");
        }
        try {
            if (type == String.class) return raw;
            if (type == int.class || type == Integer.class) return Integer.parseInt(raw);
            if (type == long.class || type == Long.class) return Long.parseLong(raw);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(raw);
            if (type.isEnum()) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Object value = Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), raw.toUpperCase());
                return value;
            }
        } catch (Exception exception) {
            throw new InteractionBindingException("Cannot convert value '" + raw + "' for parameter '" + key + "' to " + type.getSimpleName());
        }
        throw new InteractionBindingException("Unsupported parameter type for path param: " + type.getName());
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof InvocationTargetException invocationTargetException && invocationTargetException.getCause() != null) {
            return invocationTargetException.getCause();
        }
        return throwable;
    }

    private static InteractionContext baseContext(InteractionPayload payload) {
        return switch (payload.type()) {
            case MODAL -> new ModalContext(payload);
            case BUTTON -> new ComponentContext(payload);
            case STRING_SELECT, USER_SELECT, ROLE_SELECT, MENTIONABLE_SELECT, CHANNEL_SELECT -> new SelectContext(payload);
        };
    }

    public InteractionRouteRegistry routeRegistry() { return routeRegistry; }
    public SessionRegistry sessions() { return sessions; }
    public StateCodecRegistry stateCodecs() { return stateCodecs; }
}
