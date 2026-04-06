package com.github.cybellereaper.examples.interactions;

import com.github.cybellereaper.interactions.core.annotation.*;
import com.github.cybellereaper.interactions.core.context.ComponentContext;
import com.github.cybellereaper.interactions.core.context.ModalContext;
import com.github.cybellereaper.interactions.core.context.SelectContext;
import com.github.cybellereaper.interactions.core.response.InteractionResponse;

import java.util.List;

@InteractionGroup("ticket")
public final class TicketInteractionExample {

    @Button("open")
    public InteractionResponse openTicket(ComponentContext context) {
        return InteractionResponse.openModal("ticket:create");
    }

    @Modal("create")
    @GuildOnly
    public void submitTicket(ModalContext context, @Field("subject") String subject, @Field("details") String details) {
        context.deferReply(true);
    }

    @Button("close:{ticketId}")
    @Check("staffOnly")
    public InteractionResponse closeTicket(ComponentContext context, @PathParam("ticketId") long ticketId) {
        return InteractionResponse.updateMessage().content("Ticket #" + ticketId + " closed").disableTriggeredComponent();
    }

    @StringSelect("priority")
    public void choosePriority(SelectContext context, List<String> values) {
        context.replyEphemeral("Priority set to " + values.getFirst());
    }
}
