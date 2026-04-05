package com.github.cybellereaper.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SlashCommandRouterTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void routesSlashCommandInteractionsToRegisteredHandlers() throws Exception {
        AtomicInteger invocationCount = new AtomicInteger(0);

        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> {
        });
        router.registerSlashHandler("ping", ignored -> invocationCount.incrementAndGet());

        router.handleInteraction(interactionPayload(2, "ping", null, "42", "token-value", null, 1));

        assertEquals(1, invocationCount.get());
    }

    @Test
    void routesAutocompleteInteractionsByCommandName() throws Exception {
        AtomicInteger autocompleteCount = new AtomicInteger(0);

        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> {
        });
        router.registerAutocompleteHandler("echo", ignored -> autocompleteCount.incrementAndGet());

        router.handleInteraction(interactionPayload(4, "echo", null, "42", "token-value", "he", null));

        assertEquals(1, autocompleteCount.get());
    }

    @Test
    void routesComponentAndModalInteractionsByCustomId() throws Exception {
        AtomicInteger componentCount = new AtomicInteger(0);
        AtomicInteger modalCount = new AtomicInteger(0);

        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> {
        });
        router.registerComponentHandler("confirm_button", ignored -> componentCount.incrementAndGet());
        router.registerModalHandler("feedback_modal", ignored -> modalCount.incrementAndGet());

        router.handleInteraction(interactionPayload(3, null, "confirm_button", "1", "token", null, null));
        router.handleInteraction(interactionPayload(5, null, "feedback_modal", "2", "token", null, null));

        assertEquals(1, componentCount.get());
        assertEquals(1, modalCount.get());
    }

    @Test
    void routesUserAndMessageContextMenusByCommandName() throws Exception {
        AtomicInteger userCount = new AtomicInteger(0);
        AtomicInteger messageCount = new AtomicInteger(0);

        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> {
        });
        router.registerUserContextMenuHandler("Inspect User", ignored -> userCount.incrementAndGet());
        router.registerMessageContextMenuHandler("Quote Message", ignored -> messageCount.incrementAndGet());

        router.handleInteraction(interactionPayload(2, "Inspect User", null, "42", "token-value", null, 2));
        router.handleInteraction(interactionPayload(2, "Quote Message", null, "43", "token-value", null, 3));

        assertEquals(1, userCount.get());
        assertEquals(1, messageCount.get());
    }

    @Test
    void autoRespondsToPingInteractionsWithPong() throws Exception {
        AtomicReference<Integer> responseType = new AtomicReference<>();

        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> responseType.set(type));

        router.handleInteraction(interactionPayload(1, null, null, "123", "abc", null, null));

        assertEquals(1, responseType.get());
    }

    @Test
    void respondEphemeralUsesCorrectFlags() throws Exception {
        AtomicReference<Map<String, Object>> responseData = new AtomicReference<>();

        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> responseData.set(data));

        router.respondEphemeral(interactionPayload(2, "ping", null, "123", "abc", null, 1), "hidden");

        assertEquals("hidden", responseData.get().get("content"));
        assertEquals(64, responseData.get().get("flags"));
    }

    @Test
    void respondWithEmbedsIncludesEmbedPayload() throws Exception {
        AtomicReference<Map<String, Object>> responseData = new AtomicReference<>();

        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> responseData.set(data));

        router.respondWithEmbeds(
                interactionPayload(2, "ping", null, "123", "abc", null, 1),
                "hello",
                List.of(new DiscordEmbed("Title", "Description", 12345))
        );

        assertEquals("hello", responseData.get().get("content"));
        assertTrue(responseData.get().containsKey("embeds"));
    }

    @Test
    void respondWithAutocompleteChoicesUsesCorrectResponseType() throws Exception {
        AtomicReference<Integer> responseType = new AtomicReference<>();
        AtomicReference<Map<String, Object>> responseData = new AtomicReference<>();

        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> {
            responseType.set(type);
            responseData.set(data);
        });

        router.respondWithAutocompleteChoices(
                interactionPayload(4, "echo", null, "123", "abc", "he", null),
                List.of(new AutocompleteChoice("hello", "hello"))
        );

        assertEquals(8, responseType.get());
        assertTrue(responseData.get().containsKey("choices"));
    }

    @Test
    void returnsStringOptionValueWhenPresent() throws Exception {
        JsonNode interaction = interactionPayload(2, "echo", null, "123", "abc", "hello", 1);
        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> {
        });

        assertEquals("hello", router.getOptionString(interaction, "text"));
        assertNull(router.getOptionString(interaction, "missing"));
    }

    @Test
    void rejectsDuplicateHandlerRegistration() {
        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> {
        });
        router.registerSlashHandler("ping", ignored -> {
        });

        assertThrows(IllegalArgumentException.class, () -> router.registerSlashHandler("ping", ignored -> {
        }));
    }

    @Test
    void respondWithMessageRequiresIdAndToken() throws Exception {
        SlashCommandRouter router = new SlashCommandRouter((id, token, type, data) -> {
        });

        JsonNode missingId = interactionPayload(2, "ping", null, "", "token", null, 1);
        JsonNode missingToken = interactionPayload(2, "ping", null, "id", "", null, 1);

        assertThrows(IllegalArgumentException.class, () -> router.respondWithMessage(missingId, "pong"));
        assertThrows(IllegalArgumentException.class, () -> router.respondWithMessage(missingToken, "pong"));
    }

    private static JsonNode interactionPayload(
            int type,
            String commandName,
            String customId,
            String id,
            String token,
            String optionValue,
            Integer commandType
    ) throws Exception {
        String data = switch (type) {
            case 2 -> {
                String typeField = commandType == null ? "" : "\"type\": %d, ".formatted(commandType);
                yield optionValue == null
                        ? "{%s\"name\": \"%s\"}".formatted(typeField, commandName)
                        : "{%s\"name\": \"%s\", \"options\": [{\"name\": \"text\", \"value\": \"%s\"}]}".formatted(typeField, commandName, optionValue);
            }
            case 4 -> optionValue == null
                    ? "{\"name\": \"%s\"}".formatted(commandName)
                    : "{\"name\": \"%s\", \"options\": [{\"name\": \"text\", \"value\": \"%s\"}]}".formatted(commandName, optionValue);
            case 3, 5 -> "{\"custom_id\": \"%s\"}".formatted(customId);
            default -> "{}";
        };

        String json = """
                {
                  "type": %d,
                  "id": "%s",
                  "token": "%s",
                  "data": %s
                }
                """.formatted(type, id, token, data);
        return MAPPER.readTree(json);
    }
}
