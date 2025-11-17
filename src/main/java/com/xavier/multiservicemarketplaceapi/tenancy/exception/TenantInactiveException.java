package com.xavier.multiservicemarketplaceapi.tenancy.exception;

import org.springframework.http.HttpStatus;

public class TenantInactiveException extends TenantResolutionException {

    public TenantInactiveException(String identifier) {
        super("Tenant '%s' is not active.".formatted(identifier), HttpStatus.FORBIDDEN);
    }
}

