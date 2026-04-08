package com.github.cybellereaper.medusae.commands.api;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface AnnotatedCommandRuntime {
    void registerCommands(Object... commands);

    void registerInteractionModules(Object... modules);

    void registerCheck(String name, Predicate<Object> check);

    void registerAutocomplete(String name, BiFunction<Object, String, List<String>> provider);
}
