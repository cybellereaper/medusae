package com.github.cybellereaper.interactions.core.context;

import com.github.cybellereaper.interactions.core.model.InteractionPayload;

import java.util.List;

public final class SelectContext extends InteractionContext {
    public SelectContext(InteractionPayload payload) {
        super(payload);
    }

    public List<String> values() {
        return payload().selectedValues();
    }
}
