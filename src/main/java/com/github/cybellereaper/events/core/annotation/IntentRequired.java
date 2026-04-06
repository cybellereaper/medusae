package com.github.cybellereaper.events.core.annotation;

import com.github.cybellereaper.gateway.GatewayIntent;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface IntentRequired {
    GatewayIntent[] value();
}
