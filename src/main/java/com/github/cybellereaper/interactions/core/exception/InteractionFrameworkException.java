package com.github.cybellereaper.interactions.core.exception;

public class InteractionFrameworkException extends RuntimeException {
    public InteractionFrameworkException(String message) { super(message); }
    public InteractionFrameworkException(String message, Throwable cause) { super(message, cause); }
}
