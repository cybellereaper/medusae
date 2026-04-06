package com.github.cybellereaper.events.discord.mapping;

public final class DiscordEvents {
    private DiscordEvents() {}

    public record ResumedEvent(String sessionId) {}
    public record ReconnectingEvent(String reason) {}
    public record SessionInvalidatedEvent(boolean resumable) {}
    public record MessageUpdateEvent(String id, String channelId, String guildId, String content) {}
    public record MessageDeleteEvent(String id, String channelId, String guildId) {}
    public record GuildCreateEvent(String guildId, boolean available) {}
    public record MemberJoinEvent(String guildId, String userId) {}
    public record MemberUpdateEvent(String guildId, String userId, String nick) {}
    public record MemberLeaveEvent(String guildId, String userId) {}
    public record InteractionCreateEvent(String interactionId, String type, Object payload) {}
    public record CacheReadyEvent(int guildCount) {}
}
