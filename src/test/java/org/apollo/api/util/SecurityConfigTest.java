package org.apollo.api.util;

import org.apollo.api.model.Roles;
import org.apollo.api.repository.AuthUserRepository;
import org.apollo.api.security.AuthUser;
import org.apollo.api.security.JwtAuthenticationData;
import org.apollo.api.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(classes = {SecurityConfig.class, SecurityConfigTest.TestConfiguration.class})
@WebAppConfiguration
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void shouldAllowOperatorToRegisterInverter() throws Exception {
        authenticate("operator-token", "OPERATOR");
        mockMvc.perform(post("/api/v1/inverters").header(HttpHeaders.AUTHORIZATION, "Bearer operator-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAnalystWrite() throws Exception {
        authenticate("analyst-token", "ANALYST");
        mockMvc.perform(post("/api/v1/inverters").header(HttpHeaders.AUTHORIZATION, "Bearer analyst-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAnalystToRead() throws Exception {
        authenticate("analyst-token", "ANALYST");
        mockMvc.perform(get("/api/v1/warnings").header(HttpHeaders.AUTHORIZATION, "Bearer analyst-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowTechnicianToStartMaintenance() throws Exception {
        authenticate("technician-token", "TECHNICIAN");
        mockMvc.perform(patch("/api/v1/maintenance-registers/1/start").header(HttpHeaders.AUTHORIZATION, "Bearer technician-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectOperatorStartingMaintenance() throws Exception {
        authenticate("operator-token", "OPERATOR");
        mockMvc.perform(patch("/api/v1/maintenance-registers/1/start").header(HttpHeaders.AUTHORIZATION, "Bearer operator-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectTechnicianOnEmployees() throws Exception {
        authenticate("technician-token", "TECHNICIAN");
        mockMvc.perform(get("/api/v1/employees").header(HttpHeaders.AUTHORIZATION, "Bearer technician-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForApiEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/warnings"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json("""
                        {"status": 401, "message": "Missing or invalid token"}
                        """));
    }

    @Test
    void shouldAllowManagerToWriteEmployees() throws Exception {
        authenticate("manager-token", "MANAGER");
        mockMvc.perform(post("/api/v1/employees").header(HttpHeaders.AUTHORIZATION, "Bearer manager-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowTechnicianToUseChat() throws Exception {
        authenticate("technician-token", "TECHNICIAN");
        mockMvc.perform(post("/api/v1/chat/messages").header(HttpHeaders.AUTHORIZATION, "Bearer technician-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForChat() throws Exception {
        mockMvc.perform(post("/api/v1/chat/messages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectOperatorWritingEmployees() throws Exception {
        authenticate("operator-token", "OPERATOR");
        mockMvc.perform(post("/api/v1/employees").header(HttpHeaders.AUTHORIZATION, "Bearer operator-token"))
                .andExpect(status().isForbidden());
    }

    // Regression test for the "DELETE fails with a CORS error" bug: a CORS preflight
    // (OPTIONS) request never carries an Authorization header, so it must not be
    // rejected by the security filter chain with 401/403.
    @Test
    void shouldNotRejectCorsPreflightWithoutToken() throws Exception {
        mockMvc.perform(options("/api/v1/stocks/x/1")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Preflight was rejected by security with status " + status);
                    }
                });
    }

    private void authenticate(String token, String roleName) {
        String email = roleName.toLowerCase() + "@apollo.com";
        UUID userId = UUID.randomUUID();
        AuthUser user = mock(AuthUser.class);
        when(user.getUserId()).thenReturn(userId);
        when(user.getCompanyId()).thenReturn(10L);
        when(user.getEmail()).thenReturn(email);
        when(user.isActive()).thenReturn(true);
        when(user.getRole()).thenReturn(new Roles(1L, roleName, null));
        when(jwtService.extractAuthentication(token)).thenReturn(new JwtAuthenticationData(userId, 10L, email, roleName, null));
        when(authUserRepository.findActiveByIdentity(userId, 10L, email))
                .thenReturn(Optional.of(user));
    }

    @Configuration
    @EnableWebMvc
    static class TestConfiguration {
        @Bean JwtService jwtService() { return mock(JwtService.class); }
        @Bean AuthUserRepository authUserRepository() { return mock(AuthUserRepository.class); }
        @Bean JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, AuthUserRepository authUserRepository) {
            return new JwtAuthenticationFilter(jwtService, authUserRepository);
        }
        @Bean TestController testController() { return new TestController(); }
    }

    @RestController
    static class TestController {
        @RequestMapping("/api/v1/**") String any() { return "ok"; }
    }
}