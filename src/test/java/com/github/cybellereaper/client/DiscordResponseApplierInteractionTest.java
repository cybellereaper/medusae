package com.github.cybellereaper.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cybellereaper.commands.core.response.InteractionReply;
import com.github.cybellereaper.commands.core.response.ModalReply;
import com.github.cybellereaper.commands.discord.response.DiscordResponseApplier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscordResponseApplierInteractionTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void appliesUpdateReplies() throws Exception {
        List<Integer> responseTypes = new ArrayList<>();
        InteractionContext context = InteractionContext.from(MAPPER.readTree("{" +
                "\"id\":\"i1\",\"token\":\"t1\"}"), (id, token, type, data) -> responseTypes.add(type));

        new DiscordResponseApplier(context).accept(InteractionReply.updateMessage().content("updated").build());

        assertEquals(List.of(7), responseTypes);
    }

    @Test
    void appliesModalReplies() throws Exception {
        List<Integer> responseTypes = new ArrayList<>();
        InteractionContext context = InteractionContext.from(MAPPER.readTree("{" +
                "\"id\":\"i1\",\"token\":\"t1\"}"), (id, token, type, data) -> responseTypes.add(type));

        ModalReply modalReply = ModalReply.create("ticket:create").title("Create").textInput("subject", "Subject", true).build();
        new DiscordResponseApplier(context).accept(modalReply);

        assertEquals(List.of(9), responseTypes);
    }
}
