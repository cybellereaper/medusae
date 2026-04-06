package com.github.cybellereaper.events.core.bus;

import com.github.cybellereaper.events.core.annotation.*;
import com.github.cybellereaper.events.core.model.EventListenerDefinition;
import com.github.cybellereaper.gateway.GatewayIntent;

import java.lang.reflect.Method;
import java.util.*;

public final class EventListenerParser {

    public List<EventListenerDefinition> parse(Object listener) {
        Class<?> type = listener.getClass();
        Set<String> classFilters = extractFilters(type.getAnnotationsByType(Filter.class));
        Set<GatewayIntent> classIntents = extractIntents(type.getAnnotation(IntentRequired.class));
        boolean classGuildOnly = type.isAnnotationPresent(GuildOnly.class);
        boolean classDmOnly = type.isAnnotationPresent(DmOnly.class);

        List<EventListenerDefinition> result = new ArrayList<>();
        for (Method method : type.getDeclaredMethods()) {
            Listen listen = method.getAnnotation(Listen.class);
            if (listen == null) continue;
            method.setAccessible(true);
            Set<String> filters = new LinkedHashSet<>(classFilters);
            filters.addAll(extractFilters(method.getAnnotationsByType(Filter.class)));
            Set<GatewayIntent> intents = new LinkedHashSet<>(classIntents);
            intents.addAll(extractIntents(method.getAnnotation(IntentRequired.class)));
            int order = method.isAnnotationPresent(Order.class) ? method.getAnnotation(Order.class).value() : 0;
            result.add(new EventListenerDefinition(listener, method, listen.value(), order,
                    method.isAnnotationPresent(Once.class), method.isAnnotationPresent(Async.class),
                    classGuildOnly || method.isAnnotationPresent(GuildOnly.class),
                    classDmOnly || method.isAnnotationPresent(DmOnly.class),
                    Set.copyOf(filters), Set.copyOf(intents)));
        }
        return result;
    }

    private static Set<String> extractFilters(Filter[] annotations) {
        Set<String> values = new LinkedHashSet<>();
        for (Filter annotation : annotations) values.add(annotation.value());
        return values;
    }

    private static Set<GatewayIntent> extractIntents(IntentRequired annotation) {
        if (annotation == null) return Set.of();
        return Set.of(annotation.value());
    }
}
