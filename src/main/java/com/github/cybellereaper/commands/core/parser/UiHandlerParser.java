package com.github.cybellereaper.commands.core.parser;

import com.github.cybellereaper.commands.core.annotation.*;
import com.github.cybellereaper.commands.core.exception.RegistrationException;
import com.github.cybellereaper.commands.core.interaction.*;
import com.github.cybellereaper.commands.core.model.CooldownSpec;
import com.github.cybellereaper.commands.discord.context.ComponentContext;
import com.github.cybellereaper.commands.discord.context.InteractionContextBase;
import com.github.cybellereaper.commands.discord.context.ModalContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class UiHandlerParser {

    public List<UiHandler> parse(Object module) {
        List<UiHandler> handlers = new ArrayList<>();
        for (Method method : module.getClass().getDeclaredMethods()) {
            ParsedRoute parsedRoute = parseRoute(method);
            if (parsedRoute == null) {
                continue;
            }
            method.setAccessible(true);
            List<UiParameter> parameters = new ArrayList<>();
            Parameter[] reflected = method.getParameters();
            for (int i = 0; i < reflected.length; i++) {
                parameters.add(parseParameter(i, reflected[i], parsedRoute.type));
            }
            handlers.add(new UiHandler(
                    module,
                    method,
                    parsedRoute.type,
                    UiRoute.compile(parsedRoute.template),
                    method.isAnnotationPresent(EphemeralDefault.class) || module.getClass().isAnnotationPresent(EphemeralDefault.class),
                    method.isAnnotationPresent(DeferReply.class) || module.getClass().isAnnotationPresent(DeferReply.class),
                    method.isAnnotationPresent(DeferUpdate.class) || module.getClass().isAnnotationPresent(DeferUpdate.class),
                    annotationValues(method.getAnnotation(Check.class), module.getClass().getAnnotation(Check.class)),
                    annotationValues(method.getAnnotation(RequireUserPermissions.class), module.getClass().getAnnotation(RequireUserPermissions.class)),
                    cooldown(method.getAnnotation(Cooldown.class), module.getClass().getAnnotation(Cooldown.class)),
                    List.copyOf(parameters)
            ));
        }
        return handlers;
    }

    private UiParameter parseParameter(int index, Parameter parameter, UiHandlerType type) {
        if (parameter.getType() == ComponentContext.class) {
            return new UiParameter(index, parameter, parameter.getType(), UiParameterKind.COMPONENT_CONTEXT, null, true);
        }
        if (parameter.getType() == ModalContext.class) {
            return new UiParameter(index, parameter, parameter.getType(), UiParameterKind.MODAL_CONTEXT, null, true);
        }
        if (parameter.getType() == InteractionContextBase.class) {
            return new UiParameter(index, parameter, parameter.getType(), UiParameterKind.BASE_CONTEXT, null, true);
        }
        if (parameter.getType() == Object.class) {
            return new UiParameter(index, parameter, parameter.getType(), UiParameterKind.RAW_INTERACTION, null, true);
        }
        PathParam pathParam = parameter.getAnnotation(PathParam.class);
        if (pathParam != null) {
            return new UiParameter(index, parameter, parameter.getType(), UiParameterKind.PATH_PARAM, pathParam.value(), !parameter.isAnnotationPresent(Optional.class));
        }
        Field field = parameter.getAnnotation(Field.class);
        if (field != null) {
            if (type != UiHandlerType.MODAL) {
                throw new RegistrationException("@Field is only valid on @ModalHandler parameters: " + parameter);
            }
            return new UiParameter(index, parameter, parameter.getType(), UiParameterKind.FIELD, field.value(), !parameter.isAnnotationPresent(Optional.class));
        }
        return new UiParameter(index, parameter, parameter.getType(), UiParameterKind.CUSTOM, parameter.getName(), !parameter.isAnnotationPresent(Optional.class));
    }

    private ParsedRoute parseRoute(Method method) {
        if (method.isAnnotationPresent(ButtonHandler.class)) {
            return new ParsedRoute(UiHandlerType.BUTTON, method.getAnnotation(ButtonHandler.class).value());
        }
        if (method.isAnnotationPresent(StringSelectHandler.class)) {
            return new ParsedRoute(UiHandlerType.STRING_SELECT, method.getAnnotation(StringSelectHandler.class).value());
        }
        if (method.isAnnotationPresent(UserSelectHandler.class)) {
            return new ParsedRoute(UiHandlerType.USER_SELECT, method.getAnnotation(UserSelectHandler.class).value());
        }
        if (method.isAnnotationPresent(RoleSelectHandler.class)) {
            return new ParsedRoute(UiHandlerType.ROLE_SELECT, method.getAnnotation(RoleSelectHandler.class).value());
        }
        if (method.isAnnotationPresent(MentionableSelectHandler.class)) {
            return new ParsedRoute(UiHandlerType.MENTIONABLE_SELECT, method.getAnnotation(MentionableSelectHandler.class).value());
        }
        if (method.isAnnotationPresent(ChannelSelectHandler.class)) {
            return new ParsedRoute(UiHandlerType.CHANNEL_SELECT, method.getAnnotation(ChannelSelectHandler.class).value());
        }
        if (method.isAnnotationPresent(ModalHandler.class)) {
            return new ParsedRoute(UiHandlerType.MODAL, method.getAnnotation(ModalHandler.class).value());
        }
        return null;
    }

    private static List<String> annotationValues(Check methodLevel, Check typeLevel) {
        if (methodLevel != null) {
            return normalize(methodLevel.value());
        }
        return typeLevel == null ? List.of() : normalize(typeLevel.value());
    }

    private static List<String> annotationValues(RequireUserPermissions methodLevel, RequireUserPermissions typeLevel) {
        if (methodLevel != null) {
            return normalize(methodLevel.value());
        }
        return typeLevel == null ? List.of() : normalize(typeLevel.value());
    }

    private static CooldownSpec cooldown(Cooldown methodLevel, Cooldown typeLevel) {
        Cooldown source = methodLevel != null ? methodLevel : typeLevel;
        return source == null ? null : new CooldownSpec(source.amount(), source.seconds(), source.bucket().trim().toLowerCase(Locale.ROOT));
    }

    private static List<String> normalize(String[] values) {
        return Arrays.stream(values).map(value -> value.trim().toLowerCase(Locale.ROOT)).toList();
    }

    private record ParsedRoute(UiHandlerType type, String template) {
    }
}
