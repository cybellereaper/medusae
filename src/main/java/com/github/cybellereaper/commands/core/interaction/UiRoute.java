package com.github.cybellereaper.commands.core.interaction;

import com.github.cybellereaper.commands.core.exception.RegistrationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class UiRoute {
    private final String template;
    private final List<Segment> segments;

    private UiRoute(String template, List<Segment> segments) {
        this.template = template;
        this.segments = segments;
    }

    public static UiRoute compile(String template) {
        Objects.requireNonNull(template, "template");
        String normalized = template.trim();
        if (normalized.isBlank()) {
            throw new RegistrationException("Route template must not be blank");
        }
        String[] tokens = normalized.split(":");
        List<Segment> segments = new ArrayList<>(tokens.length);
        for (String token : tokens) {
            if (token.startsWith("{") && token.endsWith("}")) {
                String name = token.substring(1, token.length() - 1).trim();
                if (name.isBlank()) {
                    throw new RegistrationException("Route template contains empty path parameter: " + normalized);
                }
                segments.add(Segment.param(name));
            } else {
                if (token.isBlank()) {
                    throw new RegistrationException("Route template contains empty static segment: " + normalized);
                }
                segments.add(Segment.literal(token));
            }
        }
        return new UiRoute(normalized, List.copyOf(segments));
    }

    public String template() {
        return template;
    }

    public boolean isTemplate() {
        return segments.stream().anyMatch(Segment::parameter);
    }

    public Map<String, String> match(String customId) {
        if (customId == null || customId.isBlank()) {
            return null;
        }
        String[] input = customId.split(":");
        if (input.length != segments.size()) {
            return null;
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < input.length; i++) {
            Segment segment = segments.get(i);
            String value = input[i];
            if (!segment.parameter()) {
                if (!segment.value().equals(value)) {
                    return null;
                }
            } else {
                params.put(segment.value(), value);
            }
        }
        return params;
    }

    public boolean conflicts(UiRoute other) {
        if (segments.size() != other.segments.size()) {
            return false;
        }
        for (int i = 0; i < segments.size(); i++) {
            Segment left = segments.get(i);
            Segment right = other.segments.get(i);
            if (!left.parameter() && !right.parameter() && !left.value().equals(right.value())) {
                return false;
            }
        }
        return true;
    }

    private record Segment(boolean parameter, String value) {
        private static Segment literal(String value) {
            return new Segment(false, value);
        }

        private static Segment param(String value) {
            return new Segment(true, value);
        }
    }
}
