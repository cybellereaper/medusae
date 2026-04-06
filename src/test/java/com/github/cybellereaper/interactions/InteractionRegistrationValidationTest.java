package com.github.cybellereaper.interactions;

import com.github.cybellereaper.interactions.core.annotation.Button;
import com.github.cybellereaper.interactions.core.execute.InteractionFramework;
import com.github.cybellereaper.interactions.core.exception.RouteRegistrationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InteractionRegistrationValidationTest {

    static final class ConflictingHandlers {
        @Button("ticket:close:{id}")
        void a() {}

        @Button("ticket:close:{ticketId}")
        void b() {}
    }

    @Test
    void failsFastOnConflictingRoutes() {
        InteractionFramework framework = new InteractionFramework();
        assertThrows(RouteRegistrationException.class, () -> framework.registerInteractions(new ConflictingHandlers()));
    }
}
