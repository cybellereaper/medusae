package com.github.cybellereaper.interactions.core.response;

import java.util.Objects;

public final class InteractionResponse {
    private final InteractionResponseType type;
    private final String content;
    private final boolean ephemeral;
    private final boolean disableTriggeredComponent;
    private final ModalDefinition modal;

    private InteractionResponse(InteractionResponseType type, String content, boolean ephemeral, boolean disableTriggeredComponent, ModalDefinition modal) {
        this.type = type;
        this.content = content;
        this.ephemeral = ephemeral;
        this.disableTriggeredComponent = disableTriggeredComponent;
        this.modal = modal;
    }

    public static InteractionResponse none() { return new InteractionResponse(InteractionResponseType.NONE, null, false, false, null); }
    public static InteractionResponse reply(String content) { return new InteractionResponse(InteractionResponseType.REPLY, content, false, false, null); }
    public static InteractionResponse deferReply(boolean ephemeral) { return new InteractionResponse(InteractionResponseType.DEFER_REPLY, null, ephemeral, false, null); }
    public static InteractionResponse deferUpdate() { return new InteractionResponse(InteractionResponseType.DEFER_UPDATE, null, false, false, null); }
    public static InteractionResponse updateMessage() { return new InteractionResponse(InteractionResponseType.UPDATE_MESSAGE, null, false, false, null); }
    public static InteractionResponse followUp(String content) { return new InteractionResponse(InteractionResponseType.FOLLOW_UP, content, false, false, null); }
    public static InteractionResponse openModal(String customId) { return new InteractionResponse(InteractionResponseType.MODAL, null, false, false, new ModalDefinition(customId)); }

    public InteractionResponse content(String content) { return new InteractionResponse(type, content, ephemeral, disableTriggeredComponent, modal); }
    public InteractionResponse ephemeral(boolean ephemeral) { return new InteractionResponse(type, content, ephemeral, disableTriggeredComponent, modal); }
    public InteractionResponse disableTriggeredComponent() { return new InteractionResponse(type, content, ephemeral, true, modal); }
    public InteractionResponse modalTitle(String title) {
        Objects.requireNonNull(modal, "modal").title(title);
        return this;
    }
    public ModalDefinition modal() { return modal; }
    public InteractionResponseType type() { return type; }
    public String content() { return content; }
    public boolean ephemeral() { return ephemeral; }
    public boolean triggeredComponentDisabled() { return disableTriggeredComponent; }
}
