package com.github.cybellereaper.interactions.core.execute;

import com.github.cybellereaper.interactions.core.annotation.*;
import com.github.cybellereaper.interactions.core.context.*;
import com.github.cybellereaper.interactions.core.exception.InteractionBindingException;
import com.github.cybellereaper.interactions.core.exception.RouteRegistrationException;
import com.github.cybellereaper.interactions.core.model.*;
import com.github.cybellereaper.interactions.core.route.RouteTemplate;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class InteractionParser {

    public List<InteractionHandlerDefinition> parse(Object handlerInstance) {
        Class<?> type = handlerInstance.getClass();
        String groupPrefix = type.isAnnotationPresent(InteractionGroup.class) ? type.getAnnotation(InteractionGroup.class).value() + ":" : "";
        List<String> classChecks = extractChecks(type.getAnnotationsByType(Check.class));
        boolean classGuildOnly = type.isAnnotationPresent(GuildOnly.class);
        boolean classDmOnly = type.isAnnotationPresent(DmOnly.class);
        boolean classEphemeral = type.isAnnotationPresent(EphemeralDefault.class);
        Duration classCooldown = type.isAnnotationPresent(Cooldown.class) ? duration(type.getAnnotation(Cooldown.class)) : null;

        List<InteractionHandlerDefinition> handlers = new ArrayList<>();
        for (Method method : type.getDeclaredMethods()) {
            InteractionType interactionType = interactionType(method);
            if (interactionType == null) {
                continue;
            }
            method.setAccessible(true);
            String route = routeValue(method, interactionType);
            RouteTemplate template = RouteTemplate.compile(groupPrefix + route);
            List<InteractionParameter> parameters = parseParameters(method);
            List<String> checks = new ArrayList<>(classChecks);
            checks.addAll(extractChecks(method.getAnnotationsByType(Check.class)));
            boolean guildOnly = classGuildOnly || method.isAnnotationPresent(GuildOnly.class);
            boolean dmOnly = classDmOnly || method.isAnnotationPresent(DmOnly.class);
            if (guildOnly && dmOnly) {
                throw new RouteRegistrationException("Handler cannot be both @GuildOnly and @DmOnly: " + method);
            }
            boolean ephemeral = classEphemeral || method.isAnnotationPresent(EphemeralDefault.class);
            Duration cooldown = method.isAnnotationPresent(Cooldown.class) ? duration(method.getAnnotation(Cooldown.class)) : classCooldown;
            int priority = method.isAnnotationPresent(Priority.class) ? method.getAnnotation(Priority.class).value() : 0;
            handlers.add(new InteractionHandlerDefinition(handlerInstance, method, interactionType, template, parameters, List.copyOf(checks), guildOnly, dmOnly,
                    ephemeral, method.isAnnotationPresent(DeferReply.class), method.isAnnotationPresent(DeferUpdate.class), cooldown, priority));
        }
        return handlers;
    }

    private static List<InteractionParameter> parseParameters(Method method) {
        List<InteractionParameter> result = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            Class<?> type = parameter.getType();
            if (InteractionContext.class.isAssignableFrom(type)) {
                result.add(new InteractionParameter(index, type, ParameterBindingKind.CONTEXT, null));
                continue;
            }
            if (type == List.class) {
                result.add(new InteractionParameter(index, type, ParameterBindingKind.SELECTED_VALUES, null));
                continue;
            }
            if (type == String.class && parameter.isAnnotationPresent(Field.class)) {
                result.add(new InteractionParameter(index, type, ParameterBindingKind.MODAL_FIELD, parameter.getAnnotation(Field.class).value()));
                continue;
            }
            if (parameter.isAnnotationPresent(PathParam.class)) {
                result.add(new InteractionParameter(index, type, ParameterBindingKind.PATH_PARAM, parameter.getAnnotation(PathParam.class).value()));
                continue;
            }
            if (type == String.class && parameter.getName().equals("userId")) {
                result.add(new InteractionParameter(index, type, ParameterBindingKind.USER_ID, null));
                continue;
            }
            throw new InteractionBindingException("Unsupported parameter '" + parameter.getName() + "' for method " + method);
        }
        return List.copyOf(result);
    }

    private static List<String> extractChecks(Check[] checks) {
        List<String> values = new ArrayList<>(checks.length);
        for (Check check : checks) {
            values.add(check.value());
        }
        return values;
    }

    private static Duration duration(Cooldown cooldown) {
        return Duration.of(cooldown.amount(), cooldown.unit());
    }

    private static String routeValue(Method method, InteractionType type) {
        return switch (type) {
            case BUTTON -> method.getAnnotation(Button.class).value();
            case STRING_SELECT -> method.getAnnotation(StringSelect.class).value();
            case USER_SELECT -> method.getAnnotation(UserSelect.class).value();
            case ROLE_SELECT -> method.getAnnotation(RoleSelect.class).value();
            case MENTIONABLE_SELECT -> method.getAnnotation(MentionableSelect.class).value();
            case CHANNEL_SELECT -> method.getAnnotation(ChannelSelect.class).value();
            case MODAL -> method.getAnnotation(Modal.class).value();
        };
    }

    private static InteractionType interactionType(Method method) {
        int count = 0;
        InteractionType type = null;
        if (method.isAnnotationPresent(Button.class)) { count++; type = InteractionType.BUTTON; }
        if (method.isAnnotationPresent(StringSelect.class)) { count++; type = InteractionType.STRING_SELECT; }
        if (method.isAnnotationPresent(UserSelect.class)) { count++; type = InteractionType.USER_SELECT; }
        if (method.isAnnotationPresent(RoleSelect.class)) { count++; type = InteractionType.ROLE_SELECT; }
        if (method.isAnnotationPresent(MentionableSelect.class)) { count++; type = InteractionType.MENTIONABLE_SELECT; }
        if (method.isAnnotationPresent(ChannelSelect.class)) { count++; type = InteractionType.CHANNEL_SELECT; }
        if (method.isAnnotationPresent(Modal.class)) { count++; type = InteractionType.MODAL; }
        if (count > 1) {
            throw new RouteRegistrationException("Method has multiple interaction annotations: " + method);
        }
        return type;
    }
}
