package com.github.cybellereaper.interactions.core.route;

import java.util.Map;

public record RouteMatch(String template, Map<String, String> pathParams) {}
