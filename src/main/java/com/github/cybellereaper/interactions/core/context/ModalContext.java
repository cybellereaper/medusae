package com.github.cybellereaper.interactions.core.context;

import com.github.cybellereaper.interactions.core.model.InteractionPayload;

public final class ModalContext extends InteractionContext {
    public ModalContext(InteractionPayload payload) {
        super(payload);
    }

    public String field(String id) {
        return payload().modalField(id).orElse(null);
    }
}
