package com.github.cybellereaper.events.core.intent;

import com.github.cybellereaper.gateway.GatewayIntent;

import java.util.Set;

public record IntentDiagnostic(String listener, Set<GatewayIntent> missingIntents) {}
