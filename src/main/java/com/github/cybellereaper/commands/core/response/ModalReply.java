package com.github.cybellereaper.commands.core.response;

import com.github.cybellereaper.client.DiscordActionRow;
import com.github.cybellereaper.client.DiscordModal;
import com.github.cybellereaper.client.DiscordTextInput;

import java.util.ArrayList;
import java.util.List;

public final class ModalReply implements CommandResponse {
    private final String customId;
    private final String title;
    private final List<DiscordTextInput> fields;

    private ModalReply(Builder builder) {
        this.customId = builder.customId;
        this.title = builder.title;
        this.fields = List.copyOf(builder.fields);
    }

    public static Builder create(String customId) {
        return new Builder(customId);
    }

    public String customId() {
        return customId;
    }

    public DiscordModal toModal() {
        List<DiscordActionRow> rows = fields.stream().map(input -> DiscordActionRow.of(List.of(input))).toList();
        return DiscordModal.of(customId, title, rows);
    }

    public static final class Builder {
        private final String customId;
        private String title = "Modal";
        private final List<DiscordTextInput> fields = new ArrayList<>();

        private Builder(String customId) {
            this.customId = customId;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder textInput(String fieldId, String label, boolean required) {
            DiscordTextInput input = DiscordTextInput.shortInput(fieldId, label);
            if (!required) {
                input = input.optional();
            }
            fields.add(input);
            return this;
        }

        public Builder paragraphInput(String fieldId, String label, boolean required) {
            DiscordTextInput input = DiscordTextInput.paragraph(fieldId, label);
            if (!required) {
                input = input.optional();
            }
            fields.add(input);
            return this;
        }

        public ModalReply build() {
            return new ModalReply(this);
        }
    }
}
