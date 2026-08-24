package org.apollo.api.util;

import org.apollo.api.model.Roles;
import org.apollo.api.model.User;
import org.apollo.api.repository.UserRepository;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(classes = {SecurityConfig.class, SecurityConfigTest.TestConfiguration.class})
@WebAppConfiguration
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

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
    void shouldAllowOperatorToWrite() throws Exception {
        authenticate("operator-token", "OPERADOR");

        mockMvc.perform(post("/api/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer operator-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAnalystToRead() throws Exception {
        authenticate("analyst-token", "ANALISTA");

        mockMvc.perform(get("/api/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer analyst-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAnalystWrite() throws Exception {
        authenticate("analyst-token", "ANALISTA");

        mockMvc.perform(post("/api/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer analyst-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectTechnicianFromCrudEndpoints() throws Exception {
        authenticate("technician-token", "TECNICO");

        mockMvc.perform(get("/api/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer technician-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForCrudEndpoints() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized());
    }

    private void authenticate(String token, String roleName) {
        String email = roleName.toLowerCase() + "@apollo.com";
        User user = new User(
                1L,
                new Roles(1L, roleName, null),
                roleName,
                email,
                "encoded-password",
                "12345678901"
        );
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    }

    @Configuration
    @EnableWebMvc
    static class TestConfiguration {

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(
                JwtService jwtService,
                UserRepository userRepository
        ) {
            return new JwtAuthenticationFilter(jwtService, userRepository);
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/api/test")
        String read() {
            return "ok";
        }

        @PostMapping("/api/test")
        String write() {
            return "ok";
        }
    }
}