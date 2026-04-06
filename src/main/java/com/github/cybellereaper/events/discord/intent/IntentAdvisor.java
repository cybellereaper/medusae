package com.github.cybellereaper.events.discord.intent;

import com.github.cybellereaper.events.core.intent.IntentDiagnostic;

import java.util.List;

public final class IntentAdvisor {
    public String render(List<IntentDiagnostic> diagnostics) {
        if (diagnostics.isEmpty()) {
            return "No intent mismatches detected.";
        }
        StringBuilder builder = new StringBuilder("Intent diagnostics:\n");
        diagnostics.forEach(d -> builder.append("- ").append(d.listener()).append(" missing ").append(d.missingIntents()).append('\n'));
        return builder.toString();
    }
}
