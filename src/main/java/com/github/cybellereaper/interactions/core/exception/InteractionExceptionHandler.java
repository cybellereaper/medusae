package com.github.cybellereaper.interactions.core.exception;

import com.github.cybellereaper.interactions.core.context.InteractionContext;

@FunctionalInterface
public interface InteractionExceptionHandler {
    void onException(InteractionContext context, Throwable throwable);

    static InteractionExceptionHandler rethrowing() {
        return (ctx, throwable) -> {
            if (throwable instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new InteractionFrameworkException("Unhandled interaction error", throwable);
        };
    }
}
