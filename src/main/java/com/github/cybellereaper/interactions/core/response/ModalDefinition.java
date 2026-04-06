package com.github.cybellereaper.interactions.core.response;

import java.util.ArrayList;
import java.util.List;

public final class ModalDefinition {
    private final String customId;
    private String title = "Modal";
    private final List<TextInput> textInputs = new ArrayList<>();

    public ModalDefinition(String customId) {
        this.customId = customId;
    }

    public ModalDefinition title(String title) {
        this.title = title;
        return this;
    }

    public ModalDefinition textInput(String customId, String label, boolean required) {
        this.textInputs.add(new TextInput(customId, label, required));
        return this;
    }

    public String customId() { return customId; }
    public String title() { return title; }
    public List<TextInput> textInputs() { return List.copyOf(textInputs); }

    public record TextInput(String customId, String label, boolean required) {}
}
