package com.github.cybellereaper.medusae.commands.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Localization {
    String locale();

    String value();
}
