package com.github.cybellereaper.interactions.core.model;

public record InteractionParameter(int index, Class<?> type, ParameterBindingKind kind, String key) {}
