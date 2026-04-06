package com.github.cybellereaper.events.core.filter;

import com.github.cybellereaper.events.core.model.EventContext;

@FunctionalInterface
public interface EventFilter {
    boolean test(EventContext context);
}
