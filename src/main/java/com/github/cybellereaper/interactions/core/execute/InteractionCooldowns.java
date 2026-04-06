package com.github.cybellereaper.interactions.core.execute;

import com.github.cybellereaper.interactions.core.exception.InteractionCheckException;
import com.github.cybellereaper.interactions.core.model.InteractionHandlerDefinition;
import com.github.cybellereaper.interactions.core.model.InteractionPayload;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InteractionCooldowns {
    private final Map<String, Instant> nextAllowed = new ConcurrentHashMap<>();
    private final Clock clock;

    public InteractionCooldowns() {
        this(Clock.systemUTC());
    }

    public InteractionCooldowns(Clock clock) {
        this.clock = clock;
    }

    public void enforce(InteractionHandlerDefinition definition, InteractionPayload payload) {
        Duration cooldown = definition.cooldown();
        if (cooldown == null || cooldown.isZero() || cooldown.isNegative()) {
            return;
        }
        String key = definition.route().template() + ":" + payload.userId();
        Instant now = Instant.now(clock);
        Instant allowedAt = nextAllowed.get(key);
        if (allowedAt != null && now.isBefore(allowedAt)) {
            throw new InteractionCheckException("Cooldown active for route '" + definition.route().template() + "'");
        }
        nextAllowed.put(key, now.plus(cooldown));
    }
}
