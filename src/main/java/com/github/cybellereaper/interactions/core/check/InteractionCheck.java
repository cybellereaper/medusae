package com.github.cybellereaper.interactions.core.check;

import com.github.cybellereaper.interactions.core.context.InteractionContext;

@FunctionalInterface
public interface InteractionCheck {
    boolean test(InteractionContext context);
}
