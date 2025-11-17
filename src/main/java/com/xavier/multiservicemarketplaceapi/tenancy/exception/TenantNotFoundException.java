package com.xavier.multiservicemarketplaceapi.tenancy.exception;

import org.springframework.http.HttpStatus;

public class TenantNotFoundException extends TenantResolutionException {

    public TenantNotFoundException(String identifier) {
        super("Tenant '%s' was not found.".formatted(identifier), HttpStatus.NOT_FOUND);
    }
}

