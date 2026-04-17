package com.github.cybellereaper.medusae.commands.discord;

import com.github.cybellereaper.medusae.client.SlashCommandDefinition;
import com.github.cybellereaper.medusae.client.SlashCommandOptionDefinition;
import com.github.cybellereaper.medusae.commands.core.annotation.*;
import com.github.cybellereaper.medusae.commands.core.model.CommandType;
import com.github.cybellereaper.medusae.commands.core.parser.CommandParser;
import com.github.cybellereaper.medusae.commands.discord.schema.DiscordCommandSchemaExporter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DiscordSchemaExporterTest {
    @Test
    void exportsContextMenusAndSubcommands() {
        CommandParser parser = new CommandParser();
        DiscordCommandSchemaExporter exporter = new DiscordCommandSchemaExporter();

        SlashCommandDefinition slash = exporter.exportDefinition(parser.parse(new SubcommandCommand()));
        assertEquals(SlashCommandDefinition.CHAT_INPUT, slash.type());
        assertEquals(SlashCommandOptionDefinition.SUBCOMMAND, slash.options().getFirst().type());

        SlashCommandDefinition user = exporter.exportDefinition(parser.parse(new UserMenuCommand()));
        assertEquals(SlashCommandDefinition.USER, user.type());
    }

    @Test
    void exportsJavaOptionalAsNonRequiredOption() {
        CommandParser parser = new CommandParser();
        DiscordCommandSchemaExporter exporter = new DiscordCommandSchemaExporter();

        SlashCommandDefinition slash = exporter.exportDefinition(parser.parse(new OptionalCommand()));
        SlashCommandOptionDefinition option = slash.options().getFirst();
        assertEquals("reason", option.name());
        assertEquals(SlashCommandOptionDefinition.STRING, option.type());
        assertFalse(option.required());
    }

    @Test
    void exportsCommandRegistrationMetadata() {
        CommandParser parser = new CommandParser();
        DiscordCommandSchemaExporter exporter = new DiscordCommandSchemaExporter();

        SlashCommandDefinition slash = exporter.exportDefinition(parser.parse(new MetadataCommand()));
        assertEquals("4", slash.defaultMemberPermissions());
        assertEquals(Boolean.FALSE, slash.dmPermission());
        assertEquals(Boolean.TRUE, slash.nsfw());
        assertEquals("Configuración", slash.nameLocalizations().get("es-es"));
        assertEquals("Admin settings", slash.descriptionLocalizations().get("en-us"));
        assertEquals(java.util.List.of(0, 1), slash.contexts());
    }

    @Command("mod")
    @Description("moderation")
    static final class SubcommandCommand {
        @Subcommand("ban")
        void ban(@Name("target") String target) {
        }
    }

    @Command(value = "userinfo", type = CommandType.USER_CONTEXT)
    static final class UserMenuCommand {
        @Execute
        void root() {
        }
    }

    @Command("optional")
    static final class OptionalCommand {
        @Execute
        void root(@Name("reason") Optional<String> reason) {}
    }

    @Command("settings")
    @Description("Settings")
    @DefaultMemberPermissions(4L)
    @DmPermission(false)
    @Nsfw
    @NameLocalizations({
            @Localization(locale = "en-US", value = "Settings"),
            @Localization(locale = "es-ES", value = "Configuración")
    })
    @DescriptionLocalizations({
            @Localization(locale = "en-US", value = "Admin settings")
    })
    @CommandContexts({
            com.github.cybellereaper.medusae.commands.core.model.CommandContextType.GUILD,
            com.github.cybellereaper.medusae.commands.core.model.CommandContextType.BOT_DM
    })
    static final class MetadataCommand {
        @Execute
        void root() {}
    }
}
