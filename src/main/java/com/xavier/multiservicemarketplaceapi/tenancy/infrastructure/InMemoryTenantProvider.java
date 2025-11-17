package com.xavier.multiservicemarketplaceapi.tenancy.infrastructure;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy.TenantContext;
import com.xavier.multiservicemarketplaceapi.tenancy.config.TenancyProperties;
import com.xavier.multiservicemarketplaceapi.tenancy.domain.TenantProvider;
import com.xavier.multiservicemarketplaceapi.tenancy.exception.TenantInactiveException;
import com.xavier.multiservicemarketplaceapi.tenancy.exception.TenantNotFoundException;

@Component
@ConditionalOnMissingBean(TenantProvider.class)
public class InMemoryTenantProvider implements TenantProvider {

    private static final Logger log = LoggerFactory.getLogger(InMemoryTenantProvider.class);

    private final Map<String, TenantContext> tenantsBySlug;
    private final Map<UUID, TenantContext> tenantsById;

    public InMemoryTenantProvider(TenancyProperties properties) {
        Map<String, TenantContext> slugMap = new ConcurrentHashMap<>();
        Map<UUID, TenantContext> idMap = new ConcurrentHashMap<>();
        properties.getBootstrapTenants().forEach(seed -> {
            if (!StringUtils.hasText(seed.getSlug())) {
                return;
            }
            UUID id = seed.getId() != null ? seed.getId() : UUID.nameUUIDFromBytes(seed.getSlug().getBytes(StandardCharsets.UTF_8));
            TenantContext context = new TenantContext(id, seed.getSlug(), seed.isActive());
            slugMap.put(seed.getSlug().toLowerCase(Locale.ROOT), context);
            idMap.put(id, context);
        });
        this.tenantsBySlug = Collections.unmodifiableMap(slugMap);
        this.tenantsById = Collections.unmodifiableMap(idMap);
        log.info("Loaded {} bootstrap tenants for TenantProvider.", tenantsBySlug.size());
    }

    @Override
    public TenantContext loadTenant(String identifier) {
        TenantContext context = resolve(identifier);
        if (context == null) {
            throw new TenantNotFoundException(identifier);
        }
        if (!context.active()) {
            throw new TenantInactiveException(identifier);
        }
        return context;
    }

    private TenantContext resolve(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            return null;
        }
        TenantContext context = tenantsBySlug.get(identifier.toLowerCase(Locale.ROOT));
        if (context != null) {
            return context;
        }
        try {
            UUID id = UUID.fromString(identifier);
            return tenantsById.get(id);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}

