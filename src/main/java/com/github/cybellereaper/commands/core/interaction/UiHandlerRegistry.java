package com.github.cybellereaper.commands.core.interaction;

import com.github.cybellereaper.commands.core.exception.RegistrationException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class UiHandlerRegistry {
    private final Map<UiHandlerType, List<UiHandler>> handlersByType = new EnumMap<>(UiHandlerType.class);

    public void register(UiHandler handler) {
        List<UiHandler> handlers = handlersByType.computeIfAbsent(handler.type(), ignored -> new ArrayList<>());
        for (UiHandler existing : handlers) {
            if (existing.route().conflicts(handler.route())) {
                throw new RegistrationException("Conflicting route templates for " + handler.type() + ": " + existing.route().template() + " vs " + handler.route().template());
            }
        }
        handlers.add(handler);
    }

    public Optional<ResolvedUiHandler> resolve(UiHandlerType type, String customId) {
        List<UiHandler> handlers = handlersByType.getOrDefault(type, List.of());
        for (UiHandler handler : handlers) {
            var params = handler.route().match(customId);
            if (params != null) {
                return Optional.of(new ResolvedUiHandler(handler, params));
            }
        }
        return Optional.empty();
    }

    public List<UiHandler> all() {
        return handlersByType.values().stream().flatMap(List::stream).toList();
    }

    public record ResolvedUiHandler(UiHandler handler, Map<String, String> pathParams) {
    }
}
