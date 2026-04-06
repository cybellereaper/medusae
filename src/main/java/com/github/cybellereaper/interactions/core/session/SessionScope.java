package com.github.cybellereaper.interactions.core.session;

public record SessionScope(String userId, String messageId, String channelId, String guildId, String routeTemplate) {
    public static SessionScope any() { return new SessionScope(null, null, null, null, null); }
}
