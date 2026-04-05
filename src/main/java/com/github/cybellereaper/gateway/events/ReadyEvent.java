package com.github.cybellereaper.gateway.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReadyEvent(
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("resume_gateway_url") String resumeGatewayUrl
) {
}
