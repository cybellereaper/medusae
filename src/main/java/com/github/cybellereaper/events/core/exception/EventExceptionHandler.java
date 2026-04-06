package com.github.cybellereaper.events.core.exception;

import com.github.cybellereaper.events.core.model.EventContext;

@FunctionalInterface
public interface EventExceptionHandler {
    void onException(EventContext context, Throwable throwable);

    static EventExceptionHandler rethrowing() {
        return (context, throwable) -> {
            if (throwable instanceof RuntimeException runtime) throw runtime;
            throw new EventFrameworkException("Unhandled event exception", throwable);
        };
    }
}
