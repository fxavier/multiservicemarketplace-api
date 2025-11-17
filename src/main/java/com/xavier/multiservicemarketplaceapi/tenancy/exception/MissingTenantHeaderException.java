package com.xavier.multiservicemarketplaceapi.tenancy.exception;

import org.springframework.http.HttpStatus;

public class MissingTenantHeaderException extends TenantResolutionException {

    public MissingTenantHeaderException() {
        super("X-Tenant-ID header is required.", HttpStatus.BAD_REQUEST);
    }
}

