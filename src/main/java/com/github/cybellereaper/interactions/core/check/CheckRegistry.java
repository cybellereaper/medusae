package com.github.cybellereaper.interactions.core.check;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class CheckRegistry {
    private final Map<String, InteractionCheck> checks = new ConcurrentHashMap<>();

    public void register(String id, InteractionCheck check) {
        checks.put(id, check);
    }

    public Optional<InteractionCheck> find(String id) {
        return Optional.ofNullable(checks.get(id));
    }
}
