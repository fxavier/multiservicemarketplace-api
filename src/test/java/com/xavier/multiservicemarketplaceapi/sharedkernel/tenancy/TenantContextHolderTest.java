package com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextHolderTest {

    @AfterEach
    void cleanup() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldStoreAndRetrieveContext() {
        UUID tenantId = UUID.randomUUID();
        TenantContextHolder.set(new TenantContext(tenantId, "tenant-a", true));

        assertThat(TenantContextHolder.get()).isPresent()
            .get()
            .extracting(TenantContext::tenantId)
            .isEqualTo(tenantId);

        assertThat(TenantContextHolder.requireTenantId()).isEqualTo(tenantId);
    }

    @Test
    void shouldClearContext() {
        TenantContextHolder.set(new TenantContext(UUID.randomUUID(), "tenant-a", true));
        TenantContextHolder.clear();

        assertThat(TenantContextHolder.get()).isEmpty();
        assertThrows(TenantNotResolvedException.class, TenantContextHolder::requireTenantId);
    }
}

