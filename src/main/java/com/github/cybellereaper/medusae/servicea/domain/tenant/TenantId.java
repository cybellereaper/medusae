package com.github.cybellereaper.medusae.servicea.domain.tenant;

import java.util.Objects;

/**
 * Domain-level value object representing a guild tenant id.
 */
public record TenantId(String value) {

    public TenantId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("tenant id cannot be blank");
        }
    }

    public static TenantId fromNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new TenantId(value);
    }
}
