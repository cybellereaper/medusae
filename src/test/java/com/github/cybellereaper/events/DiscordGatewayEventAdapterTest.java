package com.github.cybellereaper.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cybellereaper.events.discord.adapter.DiscordGatewayEventAdapter;
import com.github.cybellereaper.events.discord.mapping.DiscordEvents;
import com.github.cybellereaper.gateway.events.MessageCreateEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscordGatewayEventAdapterTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void mapsMessageAndLifecycleEvents() throws Exception {
        DiscordGatewayEventAdapter adapter = new DiscordGatewayEventAdapter();

        Object message = adapter.map("MESSAGE_CREATE", MAPPER.readTree("""
                {"id":"1","channel_id":"2","guild_id":"3","content":"hi","author":{"id":"4","username":"u","discriminator":"0001"}}
                """));
        Object resumed = adapter.map("RESUMED", MAPPER.readTree("{\"session_id\":\"abc\"}"));

        assertInstanceOf(MessageCreateEvent.class, message);
        assertInstanceOf(DiscordEvents.ResumedEvent.class, resumed);
    }
}
