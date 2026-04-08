package com.github.cybellereaper.medusae.examples.commands;

import com.github.cybellereaper.medusae.commands.core.execute.AnnotatedCommandBootstrap;
import com.github.cybellereaper.medusae.commands.core.execute.CommandFramework;
import com.github.cybellereaper.medusae.commands.core.model.CommandInteraction;
import com.github.cybellereaper.medusae.commands.core.model.CommandOptionValue;
import com.github.cybellereaper.medusae.commands.core.model.CommandType;
import com.github.cybellereaper.medusae.commands.core.model.InteractionHandlerType;
import com.github.cybellereaper.medusae.commands.core.response.ImmediateResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ExampleCommandBootstrapTest {

    @Test
    void registersShowcaseCommandsAndTypes() {
        CommandFramework framework = ExampleCommandBootstrap.createFramework();

        assertEquals(4, framework.registry().all().size());
        assertTrue(framework.registry().find("user").isPresent());
        assertEquals(CommandType.CHAT_INPUT, framework.registry().find("user").orElseThrow().type());
        assertEquals(CommandType.CHAT_INPUT, framework.registry().find("ticket").orElseThrow().type());

        assertEquals(CommandType.USER_CONTEXT, framework.registry().find("inspect user").orElseThrow().type());
        assertEquals(CommandType.MESSAGE_CONTEXT, framework.registry().find("quote message").orElseThrow().type());
    }

    @Test
    void registersTicketComponentAndModalInteractionHandlers() {
        CommandFramework framework = ExampleCommandBootstrap.createFramework();

        assertTrue(framework.interactionRegistry().find(InteractionHandlerType.BUTTON, "ticket:create").isPresent());
        assertTrue(framework.interactionRegistry().find(InteractionHandlerType.MODAL, "ticket:create").isPresent());
        assertTrue(framework.interactionRegistry().find(InteractionHandlerType.BUTTON, "ticket:close:42").isPresent());
    }

    @Test
    void discoversAnnotatedModuleViaServiceLoaderAndExecutesCommand() {
        CommandFramework framework = AnnotatedCommandBootstrap.createFrameworkFromClasspath();
        AtomicReference<Object> captured = new AtomicReference<>();

        framework.execute(new CommandInteraction(
                "user", CommandType.CHAT_INPUT, null, "info",
                Map.of("target", new CommandOptionValue("u-42", 6)),
                null, Map.of(), false, "g1", "caller", Set.of(), Set.of(),
                null, null, null, null, null, null,
                null,
                Map.of("u-42", new com.github.cybellereaper.medusae.client.ResolvedUser("u-42", "tester", null, false)),
                Map.of(), Map.of(), Map.of(), Map.of()
        ), captured::set);

        assertInstanceOf(ImmediateResponse.class, captured.get());
    }
}
