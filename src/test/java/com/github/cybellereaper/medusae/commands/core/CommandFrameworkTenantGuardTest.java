package com.github.cybellereaper.medusae.commands.core;

import com.github.cybellereaper.medusae.commands.core.annotation.AllowedInteractionSource;
import com.github.cybellereaper.medusae.commands.core.annotation.ButtonHandler;
import com.github.cybellereaper.medusae.commands.core.annotation.Command;
import com.github.cybellereaper.medusae.commands.core.annotation.Execute;
import com.github.cybellereaper.medusae.commands.core.exception.CheckFailedException;
import com.github.cybellereaper.medusae.commands.core.execute.CommandFramework;
import com.github.cybellereaper.medusae.commands.core.model.CommandInteraction;
import com.github.cybellereaper.medusae.commands.core.model.CommandType;
import com.github.cybellereaper.medusae.commands.core.model.InteractionExecution;
import com.github.cybellereaper.medusae.commands.core.model.InteractionHandlerType;
import com.github.cybellereaper.medusae.commands.core.model.InteractionSource;
import com.github.cybellereaper.medusae.commands.core.model.ResolvedEntities;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandFrameworkTenantGuardTest {


    @Test
    void rejectsNullTenantBoundaryGuardDependency() {
        assertThrows(NullPointerException.class, () -> new CommandFramework(null));
    }

    @Test
    void rejectsGuildInteractionWithoutGuildId() {
        CommandFramework framework = new CommandFramework();
        framework.registerModules(new GuildInteractionModule());

        assertThrows(CheckFailedException.class, () -> framework.executeInteraction(
                new InteractionExecution(
                        InteractionHandlerType.BUTTON,
                        "guild:button",
                        Map.of(),
                        null,
                        ResolvedEntities.empty(),
                        false,
                        null,
                        "user-1",
                        Set.of(),
                        Set.of(),
                        null
                ),
                ignored -> {
                }
        ));
    }

    @Test
    void allowsGuildInteractionWhenGuildIdIsPresent() {
        CommandFramework framework = new CommandFramework();
        framework.registerModules(new GuildInteractionModule());

        assertDoesNotThrow(() -> framework.executeInteraction(
                new InteractionExecution(
                        InteractionHandlerType.BUTTON,
                        "guild:button",
                        Map.of(),
                        null,
                        ResolvedEntities.empty(),
                        false,
                        "guild-1",
                        "user-1",
                        Set.of(),
                        Set.of(),
                        null
                ),
                ignored -> {
                }
        ));
    }

    @Test
    void rejectsGuildCommandWithoutGuildId() {
        CommandFramework framework = new CommandFramework();
        framework.registerCommands(new GuildCommandModule());

        assertThrows(CheckFailedException.class, () -> framework.execute(
                new CommandInteraction(
                        "guildcheck",
                        CommandType.CHAT_INPUT,
                        null,
                        null,
                        Map.of(),
                        null,
                        null,
                        false,
                        null,
                        "user-1",
                        Set.of(),
                        Set.of(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                ignored -> {
                }
        ));
    }

    @Command("guildcheck")
    static final class GuildCommandModule {
        @Execute
        String execute() {
            return "ok";
        }
    }

    static final class GuildInteractionModule {
        @ButtonHandler("guild:button")
        @AllowedInteractionSource({InteractionSource.GUILD})
        String handle() {
            return "ok";
        }
    }
}
