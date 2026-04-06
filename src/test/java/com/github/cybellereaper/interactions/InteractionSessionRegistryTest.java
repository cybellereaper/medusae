package com.github.cybellereaper.interactions;

import com.github.cybellereaper.interactions.core.model.InteractionPayload;
import com.github.cybellereaper.interactions.core.model.InteractionType;
import com.github.cybellereaper.interactions.core.session.SessionRegistry;
import com.github.cybellereaper.interactions.core.session.SessionScope;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InteractionSessionRegistryTest {

    @Test
    void expiresTimedOutSessions() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        SessionRegistry registry = new SessionRegistry(clock);
        registry.create(new SessionScope("u1", null, null, null, "ticket:close:{ticketId}"), Duration.ofSeconds(30));

        InteractionPayload payload = new InteractionPayload(InteractionType.BUTTON, "ticket:close:12", "u1", null, null, null, null, List.of(), Map.of(), null);
        assertTrue(registry.findMatching(payload, "ticket:close:{ticketId}").isPresent());

        SessionRegistry later = new SessionRegistry(Clock.offset(clock, Duration.ofMinutes(1)));
        later.create(new SessionScope("u1", null, null, null, "ticket:close:{ticketId}"), Duration.ZERO);
        later.cleanupExpired();
        assertTrue(later.findMatching(payload, "ticket:close:{ticketId}").isEmpty());
    }
}
