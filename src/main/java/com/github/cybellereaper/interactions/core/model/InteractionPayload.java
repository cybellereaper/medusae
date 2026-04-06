package com.github.cybellereaper.interactions.core.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record InteractionPayload(
        InteractionType type,
        String customId,
        String userId,
        String memberId,
        String guildId,
        String channelId,
        String messageId,
        List<String> selectedValues,
        Map<String, String> modalFields,
        Object rawInteraction
) {
    public InteractionPayload {
        selectedValues = selectedValues == null ? List.of() : List.copyOf(selectedValues);
        modalFields = modalFields == null ? Map.of() : Map.copyOf(modalFields);
    }

    public boolean dm() {
        return guildId == null || guildId.isBlank();
    }

    public Optional<String> modalField(String id) {
        return Optional.ofNullable(modalFields.get(id));
    }
}
