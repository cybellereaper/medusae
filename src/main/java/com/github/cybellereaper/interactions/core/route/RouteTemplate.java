package com.github.cybellereaper.interactions.core.route;

import com.github.cybellereaper.interactions.core.exception.RouteRegistrationException;

import java.util.*;
import java.util.regex.Pattern;

public final class RouteTemplate {
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9_]*)}");

    private final String template;
    private final List<Segment> segments;

    private RouteTemplate(String template, List<Segment> segments) {
        this.template = template;
        this.segments = segments;
    }

    public static RouteTemplate compile(String template) {
        Objects.requireNonNull(template, "template");
        if (template.isBlank()) {
            throw new RouteRegistrationException("Route template cannot be blank");
        }
        String[] parts = template.split(":", -1);
        List<Segment> segments = new ArrayList<>(parts.length);
        Set<String> names = new HashSet<>();
        for (String part : parts) {
            var matcher = PARAMETER_PATTERN.matcher(part);
            if (matcher.matches()) {
                String name = matcher.group(1);
                if (!names.add(name)) {
                    throw new RouteRegistrationException("Duplicate path parameter '" + name + "' in template '" + template + "'");
                }
                segments.add(new Segment(true, name));
            } else if (part.contains("{") || part.contains("}")) {
                throw new RouteRegistrationException("Invalid token '" + part + "' in template '" + template + "'");
            } else {
                segments.add(new Segment(false, part));
            }
        }
        return new RouteTemplate(template, List.copyOf(segments));
    }

    public Optional<RouteMatch> match(String customId) {
        if (customId == null) return Optional.empty();
        String[] parts = customId.split(":", -1);
        if (parts.length != segments.size()) return Optional.empty();
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            String value = parts[i];
            if (segment.parameter()) {
                if (value.isBlank()) return Optional.empty();
                params.put(segment.value(), value);
            } else if (!segment.value().equals(value)) {
                return Optional.empty();
            }
        }
        return Optional.of(new RouteMatch(template, params));
    }

    public boolean conflictsWith(RouteTemplate other) {
        if (segments.size() != other.segments.size()) return false;
        for (int i = 0; i < segments.size(); i++) {
            Segment left = segments.get(i);
            Segment right = other.segments.get(i);
            if (!left.parameter() && !right.parameter() && !left.value().equals(right.value())) {
                return false;
            }
        }
        return true;
    }

    public String template() { return template; }
    public List<String> parameterNames() {
        return segments.stream().filter(Segment::parameter).map(Segment::value).toList();
    }

    private record Segment(boolean parameter, String value) {}
}
