package com.xavier.multiservicemarketplaceapi.tenancy.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TenancyWebMvcConfig implements WebMvcConfigurer {

    private final TenantResolverInterceptor tenantResolverInterceptor;

    public TenancyWebMvcConfig(TenantResolverInterceptor tenantResolverInterceptor) {
        this.tenantResolverInterceptor = tenantResolverInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantResolverInterceptor).order(Ordered.HIGHEST_PRECEDENCE);
    }
}

