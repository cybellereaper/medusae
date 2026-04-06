package com.github.cybellereaper.commands.core.interaction;

import java.lang.reflect.Parameter;

public record UiParameter(
        int index,
        Parameter reflected,
        Class<?> targetType,
        UiParameterKind kind,
        String bindingKey,
        boolean required
) {
}
