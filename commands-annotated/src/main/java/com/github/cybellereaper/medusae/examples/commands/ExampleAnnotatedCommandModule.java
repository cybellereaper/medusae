package com.github.cybellereaper.medusae.examples.commands;

import com.github.cybellereaper.medusae.commands.api.AnnotatedCommandModule;
import com.github.cybellereaper.medusae.commands.api.AnnotatedCommandRuntime;
import com.github.cybellereaper.medusae.commands.core.execute.CommandContext;

import java.util.List;

public final class ExampleAnnotatedCommandModule implements AnnotatedCommandModule {
    private static final List<String> COMMON_REASONS = List.of("Spam", "Harassment", "Raid", "Scam", "Phishing");

    @Override
    public void register(AnnotatedCommandRuntime runtime) {
        runtime.registerCheck("in-guild", rawContext -> !((CommandContext) rawContext).interaction().dm());
        runtime.registerAutocomplete("common-reasons", (rawContext, input) -> {
            String prefix = input == null ? "" : input.toLowerCase();
            return COMMON_REASONS.stream()
                    .filter(reason -> reason.toLowerCase().startsWith(prefix))
                    .toList();
        });

        runtime.registerCommands(
                new UserCommands(),
                new UserProfileContextCommands(),
                new MessageContextCommands()
        );

        runtime.registerInteractionModules(new TicketInteractionCommands());
    }
}
