package com.github.cybellereaper.medusae.commands.core.execute;

import com.github.cybellereaper.medusae.commands.api.AnnotatedCommandModule;
import com.github.cybellereaper.medusae.commands.api.AnnotatedCommandRuntime;

import java.util.List;
import java.util.ServiceLoader;

public final class AnnotatedCommandBootstrap {
    private AnnotatedCommandBootstrap() {
    }

    public static CommandFramework createFrameworkFromClasspath() {
        List<AnnotatedCommandModule> modules = ServiceLoader.load(AnnotatedCommandModule.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        return createFramework(modules.toArray(AnnotatedCommandModule[]::new));
    }

    public static CommandFramework createFramework(AnnotatedCommandModule... modules) {
        CommandFramework framework = new CommandFramework();
        AnnotatedCommandRuntime runtime = new FrameworkRuntimeAdapter(framework);
        for (AnnotatedCommandModule module : modules) {
            module.register(runtime);
        }
        return framework;
    }

    private record FrameworkRuntimeAdapter(CommandFramework framework) implements AnnotatedCommandRuntime {
        @Override
        public void registerCommands(Object... commands) {
            framework.registerCommands(commands);
        }

        @Override
        public void registerInteractionModules(Object... modules) {
            framework.registerModules(modules);
        }

        @Override
        public void registerCheck(String name, java.util.function.Predicate<Object> check) {
            framework.registerCheck(name, context -> check.test(context));
        }

        @Override
        public void registerAutocomplete(String name, java.util.function.BiFunction<Object, String, java.util.List<String>> provider) {
            framework.registerAutocomplete(name, (context, input) -> provider.apply(context, input));
        }
    }
}
