package com.github.cybellereaper.commands.core.response;

import com.github.cybellereaper.client.DiscordActionRow;
import com.github.cybellereaper.client.DiscordEmbed;
import com.github.cybellereaper.client.DiscordMessage;

import java.util.ArrayList;
import java.util.List;

public final class InteractionReply implements CommandResponse {
    public enum Mode {
        IMMEDIATE,
        UPDATE,
        DEFER_REPLY,
        DEFER_UPDATE,
        FOLLOWUP
    }

    private final Mode mode;
    private final String content;
    private final List<DiscordEmbed> embeds;
    private final List<DiscordActionRow> components;
    private final boolean ephemeral;
    private final boolean disableTriggeredComponent;
    private final boolean clearComponents;

    private InteractionReply(Builder builder) {
        this.mode = builder.mode;
        this.content = builder.content;
        this.embeds = List.copyOf(builder.embeds);
        this.components = List.copyOf(builder.components);
        this.ephemeral = builder.ephemeral;
        this.disableTriggeredComponent = builder.disableTriggeredComponent;
        this.clearComponents = builder.clearComponents;
    }

    public static Builder create() { return new Builder(Mode.IMMEDIATE); }
    public static Builder ephemeral() { return new Builder(Mode.IMMEDIATE).ephemeral(true); }
    public static Builder updateMessage() { return new Builder(Mode.UPDATE); }
    public static Builder deferReply() { return new Builder(Mode.DEFER_REPLY); }
    public static Builder deferUpdate() { return new Builder(Mode.DEFER_UPDATE); }
    public static Builder followup() { return new Builder(Mode.FOLLOWUP); }

    public Mode mode() { return mode; }
    public boolean isEphemeral() { return ephemeral; }
    public boolean disableTriggeredComponent() { return disableTriggeredComponent; }
    public boolean clearComponents() { return clearComponents; }

    public DiscordMessage toMessage() {
        DiscordMessage message = new DiscordMessage(content, embeds, components, ephemeral);
        return message;
    }

    public static final class Builder {
        private final Mode mode;
        private String content;
        private final List<DiscordEmbed> embeds = new ArrayList<>();
        private final List<DiscordActionRow> components = new ArrayList<>();
        private boolean ephemeral;
        private boolean disableTriggeredComponent;
        private boolean clearComponents;

        private Builder(Mode mode) {
            this.mode = mode;
        }

        public Builder content(String content) { this.content = content; return this; }
        public Builder embed(DiscordEmbed embed) { if (embed != null) { this.embeds.add(embed); } return this; }
        public Builder embeds(List<DiscordEmbed> embeds) { if (embeds != null) { this.embeds.addAll(embeds.stream().filter(java.util.Objects::nonNull).toList()); } return this; }
        public Builder components(List<DiscordActionRow> rows) { if (rows != null) { this.components.addAll(rows.stream().filter(java.util.Objects::nonNull).toList()); } return this; }
        public Builder ephemeral(boolean value) { this.ephemeral = value; return this; }
        public Builder disableTriggeredComponent() { this.disableTriggeredComponent = true; return this; }
        public Builder clearComponents() { this.clearComponents = true; return this; }
        public InteractionReply build() { return new InteractionReply(this); }
    }
}
