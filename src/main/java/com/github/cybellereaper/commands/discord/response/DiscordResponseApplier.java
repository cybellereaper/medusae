package com.github.cybellereaper.commands.discord.response;

import com.github.cybellereaper.client.DiscordMessage;
import com.github.cybellereaper.client.InteractionContext;
import com.github.cybellereaper.commands.core.execute.CommandResponder;
import com.github.cybellereaper.commands.core.response.CommandResponse;
import com.github.cybellereaper.commands.core.response.DeferredResponse;
import com.github.cybellereaper.commands.core.response.FollowupResponse;
import com.github.cybellereaper.commands.core.response.ImmediateResponse;
import com.github.cybellereaper.commands.core.response.InteractionReply;
import com.github.cybellereaper.commands.core.response.ModalReply;

public final class DiscordResponseApplier implements CommandResponder {
    private final InteractionContext context;

    public DiscordResponseApplier(InteractionContext context) {
        this.context = context;
    }

    @Override
    public void accept(CommandResponse response) {
        if (response instanceof ImmediateResponse immediateResponse) {
            DiscordMessage message = DiscordMessage.ofContent(immediateResponse.content());
            context.respondWithMessage(immediateResponse.ephemeral() ? message.asEphemeral() : message);
            return;
        }

        if (response instanceof DeferredResponse deferredResponse) {
            if (deferredResponse.ephemeral()) {
                context.deferMessage();
            } else {
                context.deferMessage();
            }
            return;
        }

        if (response instanceof FollowupResponse followupResponse) {
            throw new UnsupportedOperationException("Follow-up responses require webhook helpers not yet exposed by DiscordClient");
        }

        if (response instanceof InteractionReply interactionReply) {
            switch (interactionReply.mode()) {
                case IMMEDIATE -> context.respondWithMessage(interactionReply.toMessage());
                case UPDATE -> context.updateMessage(interactionReply.toMessage());
                case DEFER_REPLY -> context.deferMessage();
                case DEFER_UPDATE -> context.deferUpdate();
                case FOLLOWUP -> throw new UnsupportedOperationException("Follow-up responses require webhook helpers not yet exposed by DiscordClient");
            }
            return;
        }

        if (response instanceof ModalReply modalReply) {
            context.respondWithModal(modalReply.toModal());
        }
    }
}
