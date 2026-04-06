package com.github.cybellereaper.interactions.core.state;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class StateCodecRegistry {
    private final Map<Class<?>, StateCodec<?>> codecs = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, StateCodec<T> codec) {
        codecs.put(type, codec);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<StateCodec<T>> find(Class<T> type) {
        return Optional.ofNullable((StateCodec<T>) codecs.get(type));
    }
}
