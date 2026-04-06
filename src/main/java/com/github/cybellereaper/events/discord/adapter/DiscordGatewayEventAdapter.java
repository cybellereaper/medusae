package com.github.cybellereaper.events.discord.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.cybellereaper.events.discord.mapping.DiscordEvents;
import com.github.cybellereaper.gateway.events.MessageCreateEvent;
import com.github.cybellereaper.gateway.events.ReadyEvent;

public final class DiscordGatewayEventAdapter {
    public Object map(String eventType, JsonNode payload) {
        return switch (eventType) {
            case "READY" -> new ReadyEvent(payload.path("session_id").asText(), payload.path("resume_gateway_url").asText());
            case "RESUMED" -> new DiscordEvents.ResumedEvent(payload.path("session_id").asText());
            case "RECONNECT" -> new DiscordEvents.ReconnectingEvent("gateway requested reconnect");
            case "INVALID_SESSION" -> new DiscordEvents.SessionInvalidatedEvent(payload.asBoolean(false));
            case "MESSAGE_CREATE" -> new MessageCreateEvent(payload.path("id").asText(), payload.path("channel_id").asText(),
                    payload.path("guild_id").asText(null), payload.path("content").asText(""),
                    new MessageCreateEvent.Author(payload.path("author").path("id").asText(), payload.path("author").path("username").asText(), payload.path("author").path("discriminator").asText()));
            case "MESSAGE_UPDATE" -> new DiscordEvents.MessageUpdateEvent(payload.path("id").asText(), payload.path("channel_id").asText(), payload.path("guild_id").asText(null), payload.path("content").asText(""));
            case "MESSAGE_DELETE" -> new DiscordEvents.MessageDeleteEvent(payload.path("id").asText(), payload.path("channel_id").asText(), payload.path("guild_id").asText(null));
            case "GUILD_CREATE" -> new DiscordEvents.GuildCreateEvent(payload.path("id").asText(), payload.path("unavailable").asBoolean(false));
            case "GUILD_MEMBER_ADD" -> new DiscordEvents.MemberJoinEvent(payload.path("guild_id").asText(), payload.path("user").path("id").asText());
            case "GUILD_MEMBER_UPDATE" -> new DiscordEvents.MemberUpdateEvent(payload.path("guild_id").asText(), payload.path("user").path("id").asText(), payload.path("nick").asText(null));
            case "GUILD_MEMBER_REMOVE" -> new DiscordEvents.MemberLeaveEvent(payload.path("guild_id").asText(), payload.path("user").path("id").asText());
            case "INTERACTION_CREATE" -> new DiscordEvents.InteractionCreateEvent(payload.path("id").asText(), payload.path("type").asText(), payload);
            default -> null;
        };
    }
}
