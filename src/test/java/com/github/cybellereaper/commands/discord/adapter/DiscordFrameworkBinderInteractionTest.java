package com.github.cybellereaper.commands.discord.adapter;

import com.github.cybellereaper.commands.core.model.InteractionHandler;
import com.github.cybellereaper.examples.commands.TicketInteractionCommands;
import com.github.cybellereaper.commands.core.execute.CommandFramework;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscordFrameworkBinderInteractionTest {

    @Test
    void supportsDirectBindingForStaticInteractionRoutes() {
        CommandFramework framework = new CommandFramework();
        framework.registerModules(new TicketInteractionCommands());

        List<InteractionHandler> createHandlers = framework.interactionRegistry().all().stream()
                .filter(handler -> handler.route().equals("ticket:create"))
                .toList();
        InteractionHandler closeHandler = framework.interactionRegistry().all().stream()
                .filter(handler -> handler.route().equals("ticket:close:{ticketid}"))
                .findFirst()
                .orElseThrow();

        assertEquals(2, createHandlers.size());
        assertTrue(createHandlers.stream().allMatch(DiscordFrameworkBinder::supportsDirectClientBinding));
        assertFalse(DiscordFrameworkBinder.supportsDirectClientBinding(closeHandler));
    }
}
