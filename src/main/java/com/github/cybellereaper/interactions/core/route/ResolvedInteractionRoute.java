package com.github.cybellereaper.interactions.core.route;

import com.github.cybellereaper.interactions.core.model.InteractionHandlerDefinition;

public record ResolvedInteractionRoute(InteractionHandlerDefinition definition, RouteMatch match) {}
