package com.github.cybellereaper.medusae.commands.core.model;

public enum CommandContextType {
    GUILD(0),
    BOT_DM(1),
    PRIVATE_CHANNEL(2);

    private final int apiValue;

    CommandContextType(int apiValue) {
        this.apiValue = apiValue;
    }

    public int apiValue() {
        return apiValue;
    }
}
