package com.xavier.multiservicemarketplaceapi.tenancy.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tenancy")
public class TenancyProperties {

    private List<String> ignoredPaths = new ArrayList<>(List.of("/actuator/**"));
    private List<TenantSeed> bootstrapTenants = new ArrayList<>();

    public List<String> getIgnoredPaths() {
        return ignoredPaths;
    }

    public void setIgnoredPaths(List<String> ignoredPaths) {
        this.ignoredPaths = ignoredPaths != null ? List.copyOf(ignoredPaths) : List.of();
    }

    public List<TenantSeed> getBootstrapTenants() {
        return bootstrapTenants;
    }

    public void setBootstrapTenants(List<TenantSeed> bootstrapTenants) {
        this.bootstrapTenants = bootstrapTenants != null ? List.copyOf(bootstrapTenants) : List.of();
    }

    public static class TenantSeed {
        private UUID id;
        private String slug;
        private boolean active = true;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}

