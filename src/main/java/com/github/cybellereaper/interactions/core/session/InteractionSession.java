package com.github.cybellereaper.interactions.core.session;

import java.time.Instant;
import java.util.UUID;

public final class InteractionSession {
    private final String id = UUID.randomUUID().toString();
    private final SessionScope scope;
    private final Instant expiresAt;
    private volatile boolean complete;
    private volatile boolean cancelled;

    public InteractionSession(SessionScope scope, Instant expiresAt) {
        this.scope = scope;
        this.expiresAt = expiresAt;
    }

    public String id() { return id; }
    public SessionScope scope() { return scope; }
    public Instant expiresAt() { return expiresAt; }
    public boolean expired(Instant now) { return !now.isBefore(expiresAt); }
    public boolean complete() { return complete; }
    public boolean cancelled() { return cancelled; }
    public void markComplete() { complete = true; }
    public void cancel() { cancelled = true; }
}
