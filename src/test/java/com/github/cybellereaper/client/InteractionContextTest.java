package com.github.cybellereaper.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class InteractionContextTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void readsNestedSlashCommandOptions() throws Exception {
        JsonNode interaction = MAPPER.readTree("""
                {
                  "id": "1",
                  "token": "abc",
                  "type": 2,
                  "data": {
                    "name": "admin",
                    "options": [
                      {
                        "name": "user",
                        "options": [
                          { "name": "target", "value": "alice" }
                        ]
                      }
                    ]
                  }
                }
                """);

        InteractionContext context = InteractionContext.from(interaction, (id, token, type, data) -> {
        });

        assertEquals("alice", context.optionString("target"));
        assertNull(context.optionString("missing"));
        assertEquals("alice", context.requiredOptionString("target"));
        assertThrows(IllegalArgumentException.class, () -> context.requiredOptionString("missing"));
    }

    @Test
    void validatesAutocompleteChoiceLimit() throws Exception {
        JsonNode interaction = MAPPER.readTree("""
                {
                  "id": "1",
                  "token": "abc",
                  "type": 4,
                  "data": {
                    "name": "echo"
                  }
                }
                """);

        InteractionContext context = InteractionContext.from(interaction, (id, token, type, data) -> {
        });
        List<AutocompleteChoice> choices = java.util.stream.IntStream.range(0, 26)
                .mapToObj(i -> new AutocompleteChoice("choice-" + i, "value-" + i))
                .toList();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> context.respondWithAutocompleteChoices(choices));
        assertTrue(ex.getMessage().contains("at most 25"));
    }

    @Test
    void supportsDefersAndBasicMetadata() throws Exception {
        JsonNode interaction = MAPPER.readTree("""
                {
                  "id": "9",
                  "token": "xyz",
                  "type": 5,
                  "data": {
                    "custom_id": "feedback_modal"
                  }
                }
                """);

        AtomicReference<Integer> responseType = new AtomicReference<>();
        AtomicInteger responseCount = new AtomicInteger();
        InteractionContext context = InteractionContext.from(interaction, (id, token, type, data) -> {
            responseType.set(type);
            responseCount.incrementAndGet();
        });

        assertEquals("9", context.id());
        assertEquals("xyz", context.token());
        assertEquals("feedback_modal", context.customId());
        assertEquals(5, context.interactionType());

        context.deferUpdate();
        assertEquals(6, responseType.get());
        assertEquals(1, responseCount.get());
    }

    @Test
    void parsesNumericAndBooleanOptionsSafely() throws Exception {
        JsonNode interaction = MAPPER.readTree("""
                {
                  "id": "55",
                  "token": "abc",
                  "type": 2,
                  "data": {
                    "type": 1,
                    "name": "config",
                    "options": [
                      { "name": "count", "value": 42 },
                      { "name": "enabled", "value": true },
                      { "name": "text_num", "value": "77" },
                      { "name": "text_bool", "value": "false" },
                      { "name": "invalid_num", "value": "x42" }
                    ]
                  },
                  "guild_id": "guild-1",
                  "channel_id": "chan-9",
                  "member": {
                    "user": {
                      "id": "user-3"
                    }
                  }
                }
                """);

        InteractionContext context = InteractionContext.from(interaction, (id, token, type, data) -> {
        });

        assertEquals(42L, context.optionLong("count"));
        assertEquals(42, context.optionInt("count"));
        assertEquals(77L, context.optionLong("text_num"));
        assertNull(context.optionLong("invalid_num"));
        assertEquals(true, context.optionBoolean("enabled"));
        assertEquals(false, context.optionBoolean("text_bool"));
        assertNull(context.optionBoolean("missing"));
        assertEquals("guild-1", context.guildId());
        assertEquals("chan-9", context.channelId());
        assertEquals("user-3", context.userId());
        assertEquals(1, context.commandType());
    }
}
