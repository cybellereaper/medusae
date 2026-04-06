package com.github.cybellereaper.interactions.core.route;

import com.github.cybellereaper.interactions.core.exception.RouteRegistrationException;
import com.github.cybellereaper.interactions.core.model.InteractionHandlerDefinition;
import com.github.cybellereaper.interactions.core.model.InteractionType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class InteractionRouteRegistry {
    private final Map<InteractionType, List<InteractionHandlerDefinition>> handlers = new ConcurrentHashMap<>();

    public void register(InteractionHandlerDefinition definition) {
        List<InteractionHandlerDefinition> list = handlers.computeIfAbsent(definition.type(), ignored -> new ArrayList<>());
        for (InteractionHandlerDefinition existing : list) {
            if (existing.route().conflictsWith(definition.route())) {
                throw new RouteRegistrationException("Conflicting routes for " + definition.type() + ": '"
                        + existing.route().template() + "' and '" + definition.route().template() + "'");
            }
        }
        list.add(definition);
        list.sort(Comparator.comparingInt(InteractionHandlerDefinition::priority).reversed());
    }

    public Optional<ResolvedInteractionRoute> resolve(InteractionType type, String customId) {
        return handlers.getOrDefault(type, List.of()).stream()
                .map(def -> def.route().match(customId).map(match -> new ResolvedInteractionRoute(def, match)).orElse(null))
                .filter(Objects::nonNull)
                .findFirst();
    }

    public List<InteractionHandlerDefinition> allHandlers() {
        return handlers.values().stream().flatMap(List::stream).toList();
    }
}
