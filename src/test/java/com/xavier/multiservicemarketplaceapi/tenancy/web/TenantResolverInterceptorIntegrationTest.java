package com.xavier.multiservicemarketplaceapi.tenancy.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy.TenantContext;
import com.xavier.multiservicemarketplaceapi.sharedkernel.tenancy.TenantContextHolder;
import com.xavier.multiservicemarketplaceapi.tenancy.domain.TenantProvider;
import com.xavier.multiservicemarketplaceapi.tenancy.exception.TenantInactiveException;
import com.xavier.multiservicemarketplaceapi.tenancy.exception.TenantNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class TenantResolverInterceptorIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldAcceptValidTenantHeader() throws Exception {
        mockMvc.perform(get("/tenants/current")
                .header(TenantResolverInterceptor.TENANT_HEADER, "tenant-green")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("tenant-green")));
    }

    @Test
    void shouldRejectMissingHeader() throws Exception {
        mockMvc.perform(get("/tenants/current"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInactiveTenant() throws Exception {
        mockMvc.perform(get("/tenants/current")
                .header(TenantResolverInterceptor.TENANT_HEADER, "tenant-disabled"))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldBypassIgnoredPath() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @TestConfiguration
    static class TenantProviderTestConfig {
        @Bean
        @Primary
        TenantProvider testTenantProvider() {
            return identifier -> {
                if ("tenant-green".equals(identifier)) {
                    return new TenantContext(UUID.fromString("00000000-0000-0000-0000-00000000abcd"), identifier, true);
                }
                if ("tenant-disabled".equals(identifier)) {
                    throw new TenantInactiveException(identifier);
                }
                throw new TenantNotFoundException(identifier);
            };
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    @RequestMapping("/tenants")
    static class TestController {
        @GetMapping("/current")
        Map<String, Object> currentTenant() {
            TenantContext context = TenantContextHolder.require();
            return Map.of("id", context.tenantId().toString(), "slug", context.slug());
        }
    }
}
