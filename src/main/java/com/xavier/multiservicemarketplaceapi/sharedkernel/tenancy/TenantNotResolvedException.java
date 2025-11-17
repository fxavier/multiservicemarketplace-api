package com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy;

/**
 * Raised when code tries to access the tenant context but none was resolved for the current thread.
 */
public class TenantNotResolvedException extends RuntimeException {

    public TenantNotResolvedException(String message) {
        super(message);
    }
}

