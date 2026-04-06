package com.github.cybellereaper.interactions.core.context;

import com.github.cybellereaper.interactions.core.model.InteractionPayload;
import com.github.cybellereaper.interactions.core.response.InteractionResponse;

import java.util.Objects;

public class InteractionContext {
    private final InteractionPayload payload;
    private InteractionResponse immediateResponse;

    public InteractionContext(InteractionPayload payload) {
        this.payload = Objects.requireNonNull(payload, "payload");
    }

    public InteractionPayload payload() {
        return payload;
    }

    public String userId() {
        return payload.userId();
    }

    public String guildId() {
        return payload.guildId();
    }

    public boolean isDm() {
        return payload.dm();
    }

    public void replyEphemeral(String content) {
        immediateResponse = InteractionResponse.reply(content).ephemeral(true);
    }

    public void deferReply(boolean ephemeral) {
        immediateResponse = InteractionResponse.deferReply(ephemeral);
    }

    public void deferUpdate() {
        immediateResponse = InteractionResponse.deferUpdate();
    }

    public InteractionResponse immediateResponse() {
        return immediateResponse;
    }
}
