package com.github.cybellereaper.medusae.servicea.domain.tenant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantIdTest {

    @Test
    void rejectsBlankTenantId() {
        assertThrows(IllegalArgumentException.class, () -> new TenantId(" "));
    }

    @Test
    void fromNullableReturnsNullForBlankInput() {
        assertNull(TenantId.fromNullable("  "));
    }

    @Test
    void fromNullableBuildsTenantIdForValidValue() {
        TenantId tenantId = TenantId.fromNullable("guild-123");

        assertNotNull(tenantId);
        assertEquals("guild-123", tenantId.value());
    }
}
