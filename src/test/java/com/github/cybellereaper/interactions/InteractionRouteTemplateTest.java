package com.github.cybellereaper.interactions;

import com.github.cybellereaper.interactions.core.exception.RouteRegistrationException;
import com.github.cybellereaper.interactions.core.route.RouteTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InteractionRouteTemplateTest {

    @Test
    void extractsPathParamsFromTemplateRoute() {
        RouteTemplate template = RouteTemplate.compile("ticket:close:{ticketId}");
        var match = template.match("ticket:close:42");

        assertTrue(match.isPresent());
        assertEquals("42", match.orElseThrow().pathParams().get("ticketId"));
    }

    @Test
    void detectsConflictingTemplates() {
        RouteTemplate a = RouteTemplate.compile("ticket:close:{ticketId}");
        RouteTemplate b = RouteTemplate.compile("ticket:close:{id}");
        assertTrue(a.conflictsWith(b));
    }

    @Test
    void rejectsMalformedTemplate() {
        assertThrows(RouteRegistrationException.class, () -> RouteTemplate.compile("ticket:{oops"));
    }
}
