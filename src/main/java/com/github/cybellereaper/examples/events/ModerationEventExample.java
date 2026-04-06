package com.github.cybellereaper.examples.events;

import com.github.cybellereaper.events.core.annotation.IntentRequired;
import com.github.cybellereaper.events.core.annotation.Listen;
import com.github.cybellereaper.events.discord.mapping.DiscordEvents;
import com.github.cybellereaper.gateway.GatewayIntent;
import com.github.cybellereaper.gateway.events.MessageCreateEvent;
import com.github.cybellereaper.gateway.events.ReadyEvent;

public final class ModerationEventExample {

    @Listen(MessageCreateEvent.class)
    @IntentRequired(GatewayIntent.MESSAGE_CONTENT)
    public void onMessage(MessageCreateEvent event) {
        // moderation pipeline
    }

    @Listen(DiscordEvents.MemberJoinEvent.class)
    public void onJoin(DiscordEvents.MemberJoinEvent event) {
        // onboarding flow
    }

    @Listen(ReadyEvent.class)
    public void onReady(ReadyEvent event) {
        // startup hooks
    }
}
