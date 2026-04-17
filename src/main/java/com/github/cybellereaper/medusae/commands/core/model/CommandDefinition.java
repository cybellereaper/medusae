package com.github.cybellereaper.medusae.commands.core.model;

import java.util.List;
import java.util.Map;

public record CommandDefinition(
        String name,
        CommandType type,
        String description,
        boolean hidden,
        boolean guildOnly,
        boolean dmOnly,
        List<String> checks,
        List<String> requiredUserPermissions,
        List<String> requiredBotPermissions,
        CooldownSpec cooldown,
        List<CommandHandler> handlers,
        List<AutocompleteHandler> autocompleteHandlers,
        String defaultMemberPermissions,
        Boolean dmPermission,
        Boolean nsfw,
        Map<String, String> nameLocalizations,
        Map<String, String> descriptionLocalizations,
        List<CommandContextType> contexts
) {
}
