package com.github.cybellereaper.commands.discord.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.cybellereaper.client.InteractionContext;

import java.util.Map;

public final class ComponentContext extends InteractionContextBase {
    public ComponentContext(InteractionContext discord, JsonNode raw, Map<String, String> pathParams) {
        super(discord, raw, pathParams);
    }
}
