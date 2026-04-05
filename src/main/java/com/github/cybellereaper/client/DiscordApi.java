package com.github.cybellereaper.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.cybellereaper.http.DiscordRestClient;

import java.util.Map;
import java.util.Objects;

/**
 * High-level REST API helper for common Discord resources.
 */
public final class DiscordApi {
    private final DiscordRestClient restClient;

    DiscordApi(DiscordRestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient, "restClient");
    }

    public JsonNode getCurrentApplication() {
        return restClient.getCurrentApplication();
    }

    public JsonNode getCurrentUser() {
        return restClient.request("GET", "/users/@me", null);
    }

    public JsonNode getChannel(String channelId) {
        requireNonBlank(channelId, "channelId");
        return restClient.request("GET", "/channels/" + channelId, null);
    }

    public JsonNode getGuild(String guildId) {
        requireNonBlank(guildId, "guildId");
        return restClient.request("GET", "/guilds/" + guildId, null);
    }

    public JsonNode deleteMessage(String channelId, String messageId) {
        requireNonBlank(channelId, "channelId");
        requireNonBlank(messageId, "messageId");
        return restClient.request("DELETE", "/channels/" + channelId + "/messages/" + messageId, null);
    }

    public JsonNode request(String method, String path, Map<String, Object> body) {
        requireNonBlank(method, "method");
        requireNonBlank(path, "path");
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("path must start with '/'");
        }
        return restClient.request(method, path, body);
    }

    private static void requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
