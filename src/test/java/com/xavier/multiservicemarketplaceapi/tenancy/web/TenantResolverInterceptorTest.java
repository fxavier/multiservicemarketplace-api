package com.xavier.multiservicemarketplaceapi.tenancy.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy.TenantContext;
import com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy.TenantContextHolder;
import com.xavier.multiservicemarketplaceapi.tenancy.config.TenancyProperties;
import com.xavier.multiservicemarketplaceapi.tenancy.domain.TenantProvider;
import com.xavier.multiservicemarketplaceapi.tenancy.exception.MissingTenantHeaderException;
import com.xavier.multiservicemarketplaceapi.tenancy.exception.TenantNotFoundException;

class TenantResolverInterceptorTest {

    private final TenantProvider tenantProvider = identifier -> {
        if ("tenant-a".equals(identifier)) {
            return new TenantContext(UUID.fromString("00000000-0000-0000-0000-00000000000a"), identifier, true);
        }
        throw new TenantNotFoundException(identifier);
    };

    private TenantResolverInterceptor interceptor() {
        TenancyProperties props = new TenancyProperties();
        props.setIgnoredPaths(List.of());
        return new TenantResolverInterceptor(tenantProvider, props);
    }

    @AfterEach
    void cleanup() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldResolveTenantAndPopulateContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader(TenantResolverInterceptor.TENANT_HEADER, "tenant-a");
        HttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor().preHandle(request, response, new Object());

        assertThat(proceed).isTrue();
        assertThat(TenantContextHolder.get()).isPresent()
            .get()
            .extracting(TenantContext::slug)
            .isEqualTo("tenant-a");

        interceptor().afterCompletion(request, response, new Object(), null);
        assertThat(TenantContextHolder.get()).isEmpty();
    }

    @Test
    void shouldFailWhenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        assertThrows(MissingTenantHeaderException.class,
            () -> interceptor().preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void shouldAllowIgnoredPathWithoutHeader() throws Exception {
        TenancyProperties props = new TenancyProperties();
        props.setIgnoredPaths(List.of("/actuator/**"));
        TenantResolverInterceptor interceptor = new TenantResolverInterceptor(tenantProvider, props);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");

        boolean proceed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
        assertThat(proceed).isTrue();
        assertThat(TenantContextHolder.get()).isEmpty();
    }
}

