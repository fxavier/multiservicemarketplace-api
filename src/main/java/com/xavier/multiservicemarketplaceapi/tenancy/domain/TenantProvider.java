package com.xavier.multiservicemarketplaceapi.tenancy.domain;

import com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy.TenantContext;

/**
 * Resolves and validates tenants based on identifiers provided externally (e.g. request headers).
 */
@FunctionalInterface
public interface TenantProvider {

    /**
     * Resolves a tenant from the provided identifier (slug, code, UUID).
     *
     * @param identifier incoming tenant identifier
     * @return tenant context if found and active
     */
    TenantContext loadTenant(String identifier);
}

