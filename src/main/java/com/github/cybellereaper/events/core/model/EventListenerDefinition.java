package com.github.cybellereaper.events.core.model;

import com.github.cybellereaper.gateway.GatewayIntent;

import java.lang.reflect.Method;
import java.util.Set;

public record EventListenerDefinition(
        Object instance,
        Method method,
        Class<?> eventType,
        int order,
        boolean once,
        boolean async,
        boolean guildOnly,
        boolean dmOnly,
        Set<String> filters,
        Set<GatewayIntent> requiredIntents
) {}
