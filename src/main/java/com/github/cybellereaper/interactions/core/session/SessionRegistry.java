package com.github.cybellereaper.interactions.core.session;

import com.github.cybellereaper.interactions.core.model.InteractionPayload;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionRegistry {
    private final Map<String, InteractionSession> sessions = new ConcurrentHashMap<>();
    private final Clock clock;

    public SessionRegistry() {
        this(Clock.systemUTC());
    }

    public SessionRegistry(Clock clock) {
        this.clock = clock;
    }

    public InteractionSession create(SessionScope scope, Duration timeout) {
        InteractionSession session = new InteractionSession(scope, Instant.now(clock).plus(timeout));
        sessions.put(session.id(), session);
        return session;
    }

    public Optional<InteractionSession> findMatching(InteractionPayload payload, String routeTemplate) {
        cleanupExpired();
        return sessions.values().stream()
                .filter(session -> !session.complete() && !session.cancelled())
                .filter(session -> matches(session.scope(), payload, routeTemplate))
                .findFirst();
    }

    public void cleanupExpired() {
        Instant now = Instant.now(clock);
        sessions.entrySet().removeIf(entry -> entry.getValue().expired(now));
    }

    private static boolean matches(SessionScope scope, InteractionPayload payload, String routeTemplate) {
        return matchesPart(scope.userId(), payload.userId())
                && matchesPart(scope.messageId(), payload.messageId())
                && matchesPart(scope.channelId(), payload.channelId())
                && matchesPart(scope.guildId(), payload.guildId())
                && matchesPart(scope.routeTemplate(), routeTemplate);
    }

    private static boolean matchesPart(String expected, String actual) {
        return expected == null || Objects.equals(expected, actual);
    }
}
