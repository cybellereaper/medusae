package com.github.cybellereaper.gateway.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageCreateEvent(
        String id,
        @JsonProperty("channel_id") String channelId,
        @JsonProperty("guild_id") String guildId,
        String content,
        Author author
) {
    public record Author(
            String id,
            String username,
            String discriminator
    ) {
    }
}
