package com.github.cybellereaper.events;

import com.github.cybellereaper.events.core.annotation.IntentRequired;
import com.github.cybellereaper.events.core.annotation.Listen;
import com.github.cybellereaper.events.core.annotation.Order;
import com.github.cybellereaper.events.core.bus.EventFramework;
import com.github.cybellereaper.events.core.model.EventContext;
import com.github.cybellereaper.gateway.GatewayIntent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventFrameworkTest {

    static final class OrderedListeners {
        final List<String> order = new ArrayList<>();

        @Listen(String.class)
        @Order(10)
        void high(String event) { order.add("high"); }

        @Listen(String.class)
        @Order(1)
        void low(String event) { order.add("low"); }
    }

    static final class IntentListener {
        @Listen(String.class)
        @IntentRequired(GatewayIntent.MESSAGE_CONTENT)
        void onEvent(String event) {}
    }

    @Test
    void dispatchesListenersInDeterministicOrder() {
        EventFramework framework = new EventFramework(Set.of());
        OrderedListeners listeners = new OrderedListeners();
        framework.registerListeners(listeners);

        framework.dispatch("hello");

        assertEquals(List.of("high", "low"), listeners.order);
    }

    @Test
    void reportsMissingIntentDiagnostics() {
        EventFramework framework = new EventFramework(Set.of(GatewayIntent.GUILDS));
        framework.registerListeners(new IntentListener());

        var diagnostics = framework.intentDiagnostics();

        assertEquals(1, diagnostics.size());
        assertTrue(diagnostics.getFirst().missingIntents().contains(GatewayIntent.MESSAGE_CONTENT));
    }

    @Test
    void routesExceptionsToHandler() {
        EventFramework framework = new EventFramework(Set.of());
        framework.registerListeners(new Object() {
            @Listen(String.class)
            void boom(String event) { throw new IllegalStateException("boom"); }
        });
        List<String> errors = new ArrayList<>();
        framework.setExceptionHandler((context, throwable) -> errors.add(throwable.getMessage()));

        framework.dispatch(new EventContext("x", "raw"));

        assertEquals(List.of("boom"), errors);
    }
}
