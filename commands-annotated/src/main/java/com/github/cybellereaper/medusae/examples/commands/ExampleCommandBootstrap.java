package com.github.cybellereaper.medusae.examples.commands;

import com.github.cybellereaper.medusae.commands.core.execute.AnnotatedCommandBootstrap;
import com.github.cybellereaper.medusae.commands.core.execute.CommandFramework;

public final class ExampleCommandBootstrap {
    private ExampleCommandBootstrap() {
    }

    public static CommandFramework createFramework() {
        return AnnotatedCommandBootstrap.createFramework(new ExampleAnnotatedCommandModule());
    }
}
