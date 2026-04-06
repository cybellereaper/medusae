package com.github.cybellereaper.events.core.exception;

public class EventFrameworkException extends RuntimeException {
    public EventFrameworkException(String message) { super(message); }
    public EventFrameworkException(String message, Throwable cause) { super(message, cause); }
}
