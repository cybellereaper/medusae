package com.github.cybellereaper.interactions;

import com.github.cybellereaper.interactions.core.annotation.*;
import com.github.cybellereaper.interactions.core.context.ComponentContext;
import com.github.cybellereaper.interactions.core.context.ModalContext;
import com.github.cybellereaper.interactions.core.context.SelectContext;
import com.github.cybellereaper.interactions.core.execute.InteractionFramework;
import com.github.cybellereaper.interactions.core.model.InteractionPayload;
import com.github.cybellereaper.interactions.core.model.InteractionType;
import com.github.cybellereaper.interactions.core.response.InteractionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class InteractionFrameworkDispatchTest {

    @InteractionGroup("ticket")
    static final class TestHandlers {
        final AtomicLong closed = new AtomicLong(-1);
        String priority;

        @Button("close:{ticketId}")
        InteractionResponse close(ComponentContext context, @PathParam("ticketId") long ticketId) {
            closed.set(ticketId);
            return InteractionResponse.updateMessage().content("closed");
        }

        @Modal("create")
        InteractionResponse modal(ModalContext context, @Field("subject") String subject) {
            return InteractionResponse.reply("subject:" + subject);
        }

        @StringSelect("priority")
        void priority(SelectContext context, List<String> values) {
            priority = values.getFirst();
            context.replyEphemeral("ok");
        }
    }

    @Test
    void dispatchesButtonTemplateWithTypedPathParam() {
        InteractionFramework framework = new InteractionFramework();
        TestHandlers handlers = new TestHandlers();
        framework.registerInteractions(handlers);

        InteractionResponse response = framework.dispatch(new InteractionPayload(InteractionType.BUTTON, "ticket:close:77",
                "u1", "u1", "g1", "c1", "m1", List.of(), Map.of(), null));

        assertEquals(77L, handlers.closed.get());
        assertEquals("closed", response.content());
    }

    @Test
    void dispatchesModalAndBindsField() {
        InteractionFramework framework = new InteractionFramework();
        framework.registerInteractions(new TestHandlers());

        InteractionResponse response = framework.dispatch(new InteractionPayload(InteractionType.MODAL, "ticket:create",
                "u1", "u1", "g1", "c1", "m1", List.of(), Map.of("subject", "help"), null));

        assertEquals("subject:help", response.content());
    }

    @Test
    void dispatchesSelectAndUsesContextReply() {
        InteractionFramework framework = new InteractionFramework();
        TestHandlers handlers = new TestHandlers();
        framework.registerInteractions(handlers);

        InteractionResponse response = framework.dispatch(new InteractionPayload(InteractionType.STRING_SELECT, "ticket:priority",
                "u1", "u1", "g1", "c1", "m1", List.of("high"), Map.of(), null));

        assertEquals("high", handlers.priority);
        assertTrue(response.ephemeral());
        assertEquals("ok", response.content());
    }
}
