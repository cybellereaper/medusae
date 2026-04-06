package com.github.cybellereaper.interactions.discord.response;

import com.github.cybellereaper.interactions.core.response.InteractionResponse;
import com.github.cybellereaper.interactions.core.response.InteractionResponseType;

import java.util.HashMap;
import java.util.Map;

public final class DiscordInteractionResponseMapper {
    public Map<String, Object> toPayload(InteractionResponse response) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", callbackType(response.type()));
        if (response.type() == InteractionResponseType.MODAL && response.modal() != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("custom_id", response.modal().customId());
            data.put("title", response.modal().title());
            payload.put("data", data);
            return payload;
        }
        if (response.content() != null || response.ephemeral()) {
            Map<String, Object> data = new HashMap<>();
            if (response.content() != null) {
                data.put("content", response.content());
            }
            if (response.ephemeral()) {
                data.put("flags", 64);
            }
            payload.put("data", data);
        }
        return payload;
    }

    private static int callbackType(InteractionResponseType type) {
        return switch (type) {
            case REPLY, FOLLOW_UP -> 4;
            case DEFER_REPLY -> 5;
            case DEFER_UPDATE -> 6;
            case UPDATE_MESSAGE, EDIT_ORIGINAL -> 7;
            case MODAL -> 9;
            case NONE -> 0;
        };
    }
}
