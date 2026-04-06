package com.github.cybellereaper.commands.core.interaction;

import com.github.cybellereaper.commands.core.model.CooldownSpec;

import java.lang.reflect.Method;
import java.util.List;

public record UiHandler(
        Object instance,
        Method method,
        UiHandlerType type,
        UiRoute route,
        boolean ephemeralDefault,
        boolean deferReply,
        boolean deferUpdate,
        List<String> checks,
        List<String> requiredUserPermissions,
        CooldownSpec cooldown,
        List<UiParameter> parameters
) {
}
