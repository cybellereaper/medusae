package com.github.cybellereaper.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cybellereaper.client.DiscordClientConfig;
import com.github.cybellereaper.client.SlashCommandDefinition;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscordRestClientTest {
    private final DiscordRestClient restClient = new DiscordRestClient(
            HttpClient.newHttpClient(),
            new ObjectMapper(),
            DiscordClientConfig.builder("token").build()
    );

    @Test
    void rejectsBlankGuildIdForGuildCommandRegistration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> restClient.createGuildApplicationCommand("app-id", " ", SlashCommandDefinition.simple("ping", "Pong"))
        );
    }

    @Test
    void rejectsBlankApplicationIdForGlobalCommandRegistration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> restClient.createGlobalApplicationCommand("", SlashCommandDefinition.simple("ping", "Pong"))
        );
    }

    @Test
    void rejectsBlankApplicationIdForBulkGlobalCommandSync() {
        assertThrows(
                IllegalArgumentException.class,
                () -> restClient.bulkOverwriteGlobalApplicationCommands("", List.of())
        );
    }

    @Test
    void rejectsBlankGuildIdForBulkGuildCommandSync() {
        assertThrows(
                IllegalArgumentException.class,
                () -> restClient.bulkOverwriteGuildApplicationCommands("app-id", " ", List.of())
        );
    }

    @Test
    void rejectsNullCommandListForBulkGlobalCommandSync() {
        assertThrows(
                NullPointerException.class,
                () -> restClient.bulkOverwriteGlobalApplicationCommands("app-id", null)
        );
    }

    @Test
    void rejectsNullCommandEntriesForBulkGlobalCommandSync() {
        assertThrows(
                NullPointerException.class,
                () -> restClient.bulkOverwriteGlobalApplicationCommands("app-id", List.of(SlashCommandDefinition.simple("ping", "Pong"), null))
        );
    }
}
