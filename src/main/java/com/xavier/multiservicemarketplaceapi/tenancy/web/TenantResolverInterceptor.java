package com.xavier.multiservicemarketplaceapi.tenancy.web;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy.TenantContext;
import com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy.TenantContextHolder;
import com.xavier.multiservicemarketplaceapi.tenancy.config.TenancyProperties;
import com.xavier.multiservicemarketplaceapi.tenancy.domain.TenantProvider;
import com.xavier.multiservicemarketplaceapi.tenancy.exception.MissingTenantHeaderException;

@Component
public class TenantResolverInterceptor implements HandlerInterceptor {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    private final TenantProvider tenantProvider;
    private final List<String> ignoredPaths;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public TenantResolverInterceptor(TenantProvider tenantProvider, TenancyProperties properties) {
        this.tenantProvider = tenantProvider;
        this.ignoredPaths = properties.getIgnoredPaths();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (shouldSkip(request)) {
            return true;
        }
        String tenantHeader = request.getHeader(TENANT_HEADER);
        if (!StringUtils.hasText(tenantHeader)) {
            throw new MissingTenantHeaderException();
        }
        TenantContext context = tenantProvider.loadTenant(tenantHeader.trim());
        TenantContextHolder.set(context);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContextHolder.clear();
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();
        return ignoredPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}

