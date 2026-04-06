package com.github.cybellereaper.interactions.core.state;

public interface StateCodec<T> {
    String encode(T value);
    T decode(String raw);
}
