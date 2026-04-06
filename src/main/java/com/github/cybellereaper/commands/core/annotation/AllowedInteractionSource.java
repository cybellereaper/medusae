package com.github.cybellereaper.commands.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AllowedInteractionSource {
    Source[] value();

    enum Source {
        GUILD,
        DM,
        ANY
    }
}
