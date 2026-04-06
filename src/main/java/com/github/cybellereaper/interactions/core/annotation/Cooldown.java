package com.github.cybellereaper.interactions.core.annotation;

import java.lang.annotation.*;
import java.time.temporal.ChronoUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Cooldown {
    long amount();
    ChronoUnit unit() default ChronoUnit.SECONDS;
}
