package org.apollo.api.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Nomes dos cargos SEM o prefixo "ROLE_" (hasRole adiciona sozinho). O banco so tem
    // funcionarios: GERENTE, OPERADOR, ANALISTA e TECNICO (ver AuthenticatedUser).
    private static final String MANAGER = "MANAGER";
    private static final String OPERATOR = "OPERATOR";
    private static final String ANALYST = "ANALYST";
    private static final String TECHNICIAN = "TECHNICIAN";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"status\":401,\"message\":\"Missing or invalid token\"}");
                }))
                .authorizeHttpRequests(authorize -> authorize
                        // ==========================================================================
                        //  1. ENDPOINTS PUBLICOS (sem autenticacao)
                        // ==========================================================================
                        // O preflight CORS (OPTIONS) nao carrega o header Authorization. Sem esta
                        // linha o Spring Security rejeita o proprio preflight com 401/403 antes de o
                        // navegador enviar o DELETE/POST/PUT/PATCH real -> vira "erro de CORS".
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()

                        // ==========================================================================
                        //  2. DADOS DE REFERENCIA / CADASTROS (somente leitura para todos os perfis)
                        // ==========================================================================
                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/**", "/api/v1/segments/**",
                                "/api/v1/maintenance-types/**", "/api/v1/parts/**", "/api/v1/panel-models/**",
                                "/api/v1/inverter-models/**").hasAnyRole(MANAGER, OPERATOR, ANALYST, TECHNICIAN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/roles/**").hasAnyRole(MANAGER, OPERATOR, ANALYST)

                        // ==========================================================================
                        //  3. FILIAIS, ENDERECOS E FUNCIONARIOS
                        // ==========================================================================
                        // Filiais nao sao criadas/excluidas pela API; o Gerente edita a propria (escopo no servico).
                        .requestMatchers(HttpMethod.PUT, "/api/v1/company-units/**").hasRole(MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/v1/company-units/**").hasAnyRole(MANAGER, OPERATOR, ANALYST, TECHNICIAN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/addresses/**").hasAnyRole(MANAGER, OPERATOR, ANALYST)
                        .requestMatchers("/api/v1/addresses/**").hasRole(MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/v1/employees/**").hasAnyRole(MANAGER, OPERATOR, ANALYST)
                        .requestMatchers("/api/v1/employees/**").hasRole(MANAGER)
                        .requestMatchers("/api/v1/phone-change-requests/**").hasRole(MANAGER)

                        // ==========================================================================
                        //  4. OPERACAO (inversores, strings, placas, alertas, OS, estoque, realocacoes)
                        // ==========================================================================
                        // Inversores: todos consultam; Operador cadastra (pr_register_inverter).
                        .requestMatchers(HttpMethod.POST, "/api/v1/inverters/**").hasRole(OPERATOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/inverters/**").hasAnyRole(MANAGER, OPERATOR, ANALYST, TECHNICIAN)

                        // Strings: saude e candidatas a realocacao; recalcular = Gerente/Analista.
                        .requestMatchers(HttpMethod.POST, "/api/v1/strings/recalculate-health").hasAnyRole(MANAGER, ANALYST)
                        .requestMatchers(HttpMethod.GET, "/api/v1/strings/**").hasAnyRole(MANAGER, OPERATOR, ANALYST, TECHNICIAN)

                        // Placas: acoes de campo (ativar/desativar/relatar problema) sao do Tecnico.
                        .requestMatchers(HttpMethod.POST, "/api/v1/panels/activate", "/api/v1/panels/deactivate",
                                "/api/v1/panels/report-issue").hasRole(TECHNICIAN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/panels/**").hasAnyRole(MANAGER, OPERATOR, ANALYST, TECHNICIAN)

                        // Alertas: leitura (os alertas nascem de triggers/procedures);
                        // o Operador abre a ordem de servico a partir de um alerta.
                        .requestMatchers(HttpMethod.POST, "/api/v1/warnings/*/service-order").hasRole(OPERATOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/warnings/**").hasAnyRole(MANAGER, OPERATOR, ANALYST, TECHNICIAN)

                        // Ordens de servico: Operador abre/cancela; Tecnico inicia/conclui e lanca pecas.
                        .requestMatchers(HttpMethod.POST, "/api/v1/maintenance-registers/*/parts").hasAnyRole(TECHNICIAN, OPERATOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/maintenance-registers").hasRole(OPERATOR)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/maintenance-registers/*/start",
                                "/api/v1/maintenance-registers/*/complete").hasRole(TECHNICIAN)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/maintenance-registers/*/cancel").hasRole(OPERATOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/maintenance-registers/**").hasAnyRole(MANAGER, OPERATOR, ANALYST, TECHNICIAN)

                        // Realocacoes: Operador sugere; Gerente aprova/rejeita e conclui.
                        .requestMatchers(HttpMethod.POST, "/api/v1/relocations/**").hasRole(OPERATOR)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/relocations/**").hasRole(MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/v1/relocations/**").hasAnyRole(MANAGER, OPERATOR, ANALYST)

                        // Estoque: Gerente e Operador gerenciam; todos consultam.
                        .requestMatchers(HttpMethod.GET, "/api/v1/stocks/**").hasAnyRole(MANAGER, OPERATOR, ANALYST, TECHNICIAN)
                        .requestMatchers("/api/v1/stocks/**").hasAnyRole(MANAGER, OPERATOR)

                        .requestMatchers("/api/v1/chat/**").hasAnyRole(MANAGER, OPERATOR, ANALYST, TECHNICIAN)

                        // Busca global.
                        .requestMatchers(HttpMethod.GET, "/api/v1/search/**").hasAnyRole(MANAGER, OPERATOR, ANALYST)

                        // ==========================================================================
                        //  5. FALLBACK — qualquer outra rota exige apenas estar autenticado
                        //     (ex.: PATCH /api/v1/auth/password = trocar a propria senha)
                        // ==========================================================================
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
