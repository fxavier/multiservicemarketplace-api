package com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy;

import java.util.Optional;
import java.util.UUID;

/**
 * Stores the tenant context for the current request/thread.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT = new InheritableThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(TenantContext tenant) {
        CONTEXT.set(tenant);
    }

    public static Optional<TenantContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static TenantContext require() {
        return get().orElseThrow(() -> new TenantNotResolvedException("Tenant context was not resolved for the current request."));
    }

    public static UUID requireTenantId() {
        return require().tenantId();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

