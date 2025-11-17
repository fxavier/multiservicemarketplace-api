package com.xavier.multiservicemarketplaceapi.tenancy.exception;

import org.springframework.http.HttpStatus;

public abstract class TenantResolutionException extends RuntimeException {

    private final HttpStatus status;

    protected TenantResolutionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

