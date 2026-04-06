package com.github.cybellereaper.events.core.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(Filters.class)
public @interface Filter { String value(); }
