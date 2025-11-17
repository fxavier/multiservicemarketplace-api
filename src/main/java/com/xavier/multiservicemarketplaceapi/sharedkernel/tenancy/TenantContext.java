package com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy;

import java.util.UUID;

/**
 * Immutable snapshot describing the currently resolved tenant.
 */
public record TenantContext(UUID tenantId, String slug, boolean active) {
}

