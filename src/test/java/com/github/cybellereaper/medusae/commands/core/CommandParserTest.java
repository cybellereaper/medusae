package com.github.cybellereaper.medusae.commands.core;

import com.github.cybellereaper.medusae.commands.core.annotation.*;
import com.github.cybellereaper.medusae.commands.core.exception.RegistrationException;
import com.github.cybellereaper.medusae.commands.core.model.CommandContextType;
import com.github.cybellereaper.medusae.commands.core.model.CommandDefinition;
import com.github.cybellereaper.medusae.commands.core.model.CommandType;
import com.github.cybellereaper.medusae.commands.core.parser.CommandParser;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
    private final CommandParser parser = new CommandParser();

    @Test
    void parsesSlashTree() {
        CommandDefinition definition = parser.parse(new AdminCommands());

        assertEquals("admin", definition.name());
        assertEquals(CommandType.CHAT_INPUT, definition.type());
        assertEquals(2, definition.handlers().size());
        assertEquals("mod/ban", definition.handlers().getFirst().routeKey());
    }

    @Test
    void rejectsDuplicateSubcommandRoutes() {
        assertThrows(RegistrationException.class, () -> parser.parse(new DuplicateRoutes()));
    }

    @Test
    void treatsJavaOptionalAsOptionalOption() {
        CommandDefinition definition = parser.parse(new OptionalCommand());
        var parameter = definition.handlers().getFirst().parameters().getFirst();

        assertFalse(parameter.required());
        assertEquals("reason", parameter.optionName());
        assertEquals(String.class, parameter.optionType());
    }

    @Test
    void parsesCommandRegistrationMetadata() {
        CommandDefinition definition = parser.parse(new MetadataCommand());

        assertEquals("8", definition.defaultMemberPermissions());
        assertEquals(Boolean.FALSE, definition.dmPermission());
        assertEquals(Boolean.TRUE, definition.nsfw());
        assertEquals("Moderation", definition.nameLocalizations().get("en-us"));
        assertEquals("Herramientas de moderación", definition.descriptionLocalizations().get("es-es"));
        assertEquals(2, definition.contexts().size());
    }

    @Command("admin")
    @Description("admin commands")
    static final class AdminCommands {
        @SubcommandGroup("mod")
        @Subcommand("ban")
        void ban(String user) {
        }

        @Subcommand("kick")
        void kick(String user) {
        }
    }

    @Command("dup")
    static final class DuplicateRoutes {
        @Subcommand("ping")
        void a() {}

        @Subcommand("ping")
        void b() {}
    }

    @Command("optional")
    static final class OptionalCommand {
        @Execute
        void root(@Name("reason") Optional<String> reason) {}
    }

    @Command("moderation")
    @Description("Moderation tools")
    @DefaultMemberPermissions(8L)
    @DmPermission(false)
    @Nsfw
    @NameLocalizations({
            @Localization(locale = "en-US", value = "Moderation"),
            @Localization(locale = "es-ES", value = "Moderación")
    })
    @DescriptionLocalizations({
            @Localization(locale = "en-US", value = "Moderation tools"),
            @Localization(locale = "es-ES", value = "Herramientas de moderación")
    })
    @CommandContexts({CommandContextType.GUILD, CommandContextType.BOT_DM})
    static final class MetadataCommand {
        @Execute
        void root() {}
    }
}
