package com.github.cybellereaper.examples.commands;

import com.github.cybellereaper.client.DiscordActionRow;
import com.github.cybellereaper.client.DiscordButton;
import com.github.cybellereaper.client.DiscordEmbed;
import com.github.cybellereaper.commands.core.annotation.ButtonHandler;
import com.github.cybellereaper.commands.core.annotation.Command;
import com.github.cybellereaper.commands.core.annotation.Field;
import com.github.cybellereaper.commands.core.annotation.ModalHandler;
import com.github.cybellereaper.commands.core.annotation.PathParam;
import com.github.cybellereaper.commands.core.response.InteractionReply;
import com.github.cybellereaper.commands.core.response.ModalReply;
import com.github.cybellereaper.commands.discord.context.ComponentContext;
import com.github.cybellereaper.commands.discord.context.DiscordCommandContext;
import com.github.cybellereaper.commands.discord.context.ModalContext;

import java.util.List;

@Command("ticket")
public final class TicketInteractionCommands {

    public InteractionReply root(DiscordCommandContext ctx) {
        return InteractionReply.ephemeral()
                .content("Open a support ticket")
                .embed(new DiscordEmbed("Support", "Choose an action below", 0x57F287))
                .components(List.of(DiscordActionRow.of(List.of(
                        DiscordButton.primary("ticket:create", "Create Ticket"),
                        DiscordButton.primary("ticket:close:42", "Close #42")
                ))))
                .build();
    }

    @ButtonHandler("ticket:create")
    public ModalReply openTicketModal(ComponentContext ctx) {
        return ModalReply.create("ticket:create")
                .title("Create Ticket")
                .textInput("subject", "Subject", true)
                .paragraphInput("details", "Details", true)
                .build();
    }

    @ModalHandler("ticket:create")
    public InteractionReply submitTicket(ModalContext ctx, @Field("subject") String subject, @Field("details") String details) {
        return InteractionReply.ephemeral().content("Ticket created: " + subject + " (" + details.length() + " chars)").build();
    }

    @ButtonHandler("ticket:close:{ticketId}")
    public InteractionReply closeTicket(ComponentContext ctx, @PathParam("ticketId") long ticketId) {
        return InteractionReply.updateMessage()
                .content("Ticket #" + ticketId + " closed")
                .disableTriggeredComponent()
                .build();
    }
}
