package com.xavier.multiservicemarketplaceapi.tenancy.web;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.xavier.multiservicemarketplaceapi.tenancy.exception.TenantResolutionException;

@ControllerAdvice
public class TenantErrorHandler {

    @ExceptionHandler(TenantResolutionException.class)
    public ResponseEntity<TenantErrorResponse> handleTenantErrors(TenantResolutionException ex) {
        TenantErrorResponse body = new TenantErrorResponse(ex.getClass().getSimpleName(), ex.getMessage(), Instant.now());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    public record TenantErrorResponse(String code, String message, Instant timestamp) {
    }
}

