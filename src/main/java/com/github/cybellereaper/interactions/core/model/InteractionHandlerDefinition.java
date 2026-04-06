package com.github.cybellereaper.interactions.core.model;

import com.github.cybellereaper.interactions.core.route.RouteTemplate;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

public record InteractionHandlerDefinition(
        Object instance,
        Method method,
        InteractionType type,
        RouteTemplate route,
        List<InteractionParameter> parameters,
        List<String> checks,
        boolean guildOnly,
        boolean dmOnly,
        boolean ephemeralDefault,
        boolean deferReply,
        boolean deferUpdate,
        Duration cooldown,
        int priority
) {}
