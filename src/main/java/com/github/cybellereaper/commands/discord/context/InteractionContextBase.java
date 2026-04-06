package com.github.cybellereaper.commands.discord.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.cybellereaper.client.InteractionContext;

import java.util.Map;

public class InteractionContextBase {
    private final InteractionContext discord;
    private final JsonNode raw;
    private final Map<String, String> pathParams;

    public InteractionContextBase(InteractionContext discord, JsonNode raw, Map<String, String> pathParams) {
        this.discord = discord;
        this.raw = raw;
        this.pathParams = pathParams == null ? Map.of() : Map.copyOf(pathParams);
    }

    public InteractionContext discord() { return discord; }

    public JsonNode rawInteraction() { return raw; }

    public Map<String, String> pathParams() { return pathParams; }
}
