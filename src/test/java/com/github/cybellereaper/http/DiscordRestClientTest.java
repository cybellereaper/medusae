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
    void rejectsBlankApplicationIdForBulkGlobalOverwrite() {
        assertThrows(
                IllegalArgumentException.class,
                () -> restClient.bulkOverwriteGlobalApplicationCommands(" ", List.of())
        );
    }

    @Test
    void rejectsBlankGuildIdForBulkGuildOverwrite() {
        assertThrows(
                IllegalArgumentException.class,
                () -> restClient.bulkOverwriteGuildApplicationCommands("app-id", "", List.of())
        );
    }
}
