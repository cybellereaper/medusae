package com.github.cybellereaper.medusae.commands.core;

import com.github.cybellereaper.medusae.commands.core.annotation.*;
import com.github.cybellereaper.medusae.commands.core.execute.CommandContext;
import com.github.cybellereaper.medusae.commands.core.execute.CommandFramework;
import com.github.cybellereaper.medusae.commands.core.model.*;
import com.github.cybellereaper.medusae.commands.core.response.CommandResponse;
import com.github.cybellereaper.medusae.commands.core.response.InteractionReply;
import com.github.cybellereaper.medusae.commands.core.response.ResponseMode;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CommandFrameworkInteractionRegressionTest {

    @Test
    void unwrapsInteractionHandlerInvocationExceptionsWithSyntheticInteractionContext() {
        CommandFramework framework = new CommandFramework();
        framework.registerModules(new ExplodingButtonModule());

        AtomicReference<Throwable> capturedError = new AtomicReference<>();
        AtomicReference<CommandContext> capturedContext = new AtomicReference<>();
        framework.setExceptionHandler((context, throwable) -> {
            capturedContext.set(context);
            capturedError.set(throwable);
        });

        framework.executeInteraction(interaction("explode:now"), response -> {
        });

        assertNotNull(capturedError.get());
        assertEquals("boom", capturedError.get().getMessage());
        assertNotNull(capturedContext.get());
        assertEquals("interaction", capturedContext.get().interaction().commandName());
        assertEquals("user-1", capturedContext.get().interaction().userId());
        assertEquals("guild-1", capturedContext.get().interaction().guildId());
        assertEquals(true, capturedContext.get().interaction().dm());
    }

    @Test
    void producesIdenticalCommandAndInteractionCooldownKeys() throws Exception {
        CommandFramework framework = new CommandFramework();
        framework.registerModules(new CooldownModule());

        framework.execute(commandInteraction("cool-root"), response -> {
        });
        framework.executeInteraction(interaction("cool:button"), response -> {
        });

        Map<String, Instant> expiry = cooldownExpiry(framework);
        assertTrue(expiry.containsKey("cool-root:root:guild-1"));
        assertTrue(expiry.containsKey("interaction:BUTTON:cool:button:guild-1"));
    }

    @Test
    void keepsDeferReplyAndDeferUpdateNormalizationBehavior() {
        CommandFramework framework = new CommandFramework();
        framework.registerModules(new DeferredModule());

        AtomicReference<CommandResponse> deferReplyResponse = new AtomicReference<>();
        framework.executeInteraction(interaction("defer:reply"), deferReplyResponse::set);
        assertInstanceOf(InteractionReply.class, deferReplyResponse.get());
        InteractionReply reply = (InteractionReply) deferReplyResponse.get();
        assertEquals(ResponseMode.DEFER_REPLY, reply.mode());
        assertTrue(reply.isEphemeral());

        AtomicReference<CommandResponse> deferUpdateResponse = new AtomicReference<>();
        framework.executeInteraction(interaction("defer:update"), deferUpdateResponse::set);
        assertInstanceOf(InteractionReply.class, deferUpdateResponse.get());
        InteractionReply update = (InteractionReply) deferUpdateResponse.get();
        assertEquals(ResponseMode.DEFER_UPDATE, update.mode());
    }

    private static InteractionExecution interaction(String customId) {
        return new InteractionExecution(
                InteractionHandlerType.BUTTON,
                customId,
                Map.of(),
                null,
                ResolvedEntities.empty(),
                true,
                "guild-1",
                "user-1",
                Set.of(),
                Set.of(),
                null
        );
    }

    private static CommandInteraction commandInteraction(String commandName) {
        return new CommandInteraction(
                commandName,
                CommandType.CHAT_INPUT,
                null,
                null,
                Map.of(),
                null,
                Map.of(),
                false,
                "guild-1",
                "user-1",
                Set.of(),
                Set.of(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Instant> cooldownExpiry(CommandFramework framework) throws Exception {
        Field cooldownManagerField = CommandFramework.class.getDeclaredField("cooldownManager");
        cooldownManagerField.setAccessible(true);
        Object cooldownManager = cooldownManagerField.get(framework);
        Field expiryField = cooldownManager.getClass().getDeclaredField("expiry");
        expiryField.setAccessible(true);
        return (Map<String, Instant>) expiryField.get(cooldownManager);
    }

    static final class ExplodingButtonModule {
        @ButtonHandler("explode:now")
        void boom() {
            throw new IllegalStateException("boom");
        }
    }

    @Command("cool-root")
    @Cooldown(seconds = 30, bucket = "guild")
    static final class CooldownModule {
        @Execute
        void run() {
        }

        @ButtonHandler("cool:button")
        @Cooldown(seconds = 30, bucket = "guild")
        void onButton() {
        }
    }

    static final class DeferredModule {
        @ButtonHandler("defer:reply")
        @DeferReply
        @EphemeralDefault
        void deferReply() {
        }

        @ButtonHandler("defer:update")
        @DeferUpdate
        void deferUpdate() {
        }
    }
}
