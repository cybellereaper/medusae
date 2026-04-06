package com.github.cybellereaper.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cybellereaper.commands.core.annotation.ButtonHandler;
import com.github.cybellereaper.commands.core.annotation.Field;
import com.github.cybellereaper.commands.core.annotation.ModalHandler;
import com.github.cybellereaper.commands.core.annotation.PathParam;
import com.github.cybellereaper.commands.core.execute.CommandFramework;
import com.github.cybellereaper.commands.core.execute.CommandResponder;
import com.github.cybellereaper.commands.core.interaction.UiHandlerType;
import com.github.cybellereaper.commands.core.response.InteractionReply;
import com.github.cybellereaper.commands.core.response.ModalReply;
import com.github.cybellereaper.commands.discord.context.ComponentContext;
import com.github.cybellereaper.commands.discord.context.ModalContext;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class InteractionFrameworkUiTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void executesButtonRouteWithPathParam() throws Exception {
        CommandFramework framework = new CommandFramework();
        framework.registerCommands(new TicketUiModule());

        var interaction = MAPPER.readTree("""
                {"id":"1","token":"abc","type":3,"data":{"custom_id":"ticket:close:42"},"user":{"id":"u1"}}
                """);
        InteractionContext context = InteractionContext.from(interaction, (id, token, type, data) -> {});
        AtomicReference<Object> result = new AtomicReference<>();
        CommandResponder responder = result::set;

        framework.executeComponent(UiHandlerType.BUTTON, context.customId(), interaction, context, responder);

        assertInstanceOf(InteractionReply.class, result.get());
        InteractionReply reply = (InteractionReply) result.get();
        assertEquals(InteractionReply.Mode.UPDATE, reply.mode());
        assertEquals("Ticket 42 closed", reply.toMessage().content());
    }

    @Test
    void executesModalHandlerWithFieldBinding() throws Exception {
        CommandFramework framework = new CommandFramework();
        framework.registerCommands(new TicketUiModule());

        var interaction = MAPPER.readTree("""
                {"id":"1","token":"abc","type":5,"data":{"custom_id":"ticket:create","components":[{"components":[{"custom_id":"subject","value":"Billing"}]},{"components":[{"custom_id":"details","value":"Need help"}]}]},"user":{"id":"u1"}}
                """);
        InteractionContext context = InteractionContext.from(interaction, (id, token, type, data) -> {});
        AtomicReference<Object> result = new AtomicReference<>();

        framework.executeComponent(UiHandlerType.MODAL, context.customId(), interaction, context, result::set);

        assertInstanceOf(InteractionReply.class, result.get());
        assertEquals("Created Billing (Need help)", ((InteractionReply) result.get()).toMessage().content());
    }

    @Test
    void detectsConflictingRoutes() {
        CommandFramework framework = new CommandFramework();
        assertThrows(RuntimeException.class, () -> framework.registerCommands(new ConflictingModule()));
    }

    static final class TicketUiModule {
        @ButtonHandler("ticket:close:{ticketId}")
        public InteractionReply close(ComponentContext ctx, @PathParam("ticketId") long ticketId) {
            return InteractionReply.updateMessage().content("Ticket " + ticketId + " closed").build();
        }

        @ButtonHandler("ticket:create")
        public ModalReply open(ComponentContext ctx) {
            return ModalReply.create("ticket:create").title("Create").textInput("subject", "Subject", true).build();
        }

        @ModalHandler("ticket:create")
        public InteractionReply submit(ModalContext ctx, @Field("subject") String subject, @Field("details") String details) {
            return InteractionReply.create().content("Created " + subject + " (" + details + ")").build();
        }
    }

    static final class ConflictingModule {
        @ButtonHandler("ticket:close:{ticketId}")
        public String close1(@PathParam("ticketId") String id) { return id; }

        @ButtonHandler("ticket:close:{id}")
        public String close2(@PathParam("id") String id) { return id; }
    }
}
