package org.apollo.api.service;

import org.apollo.api.dto.LoginRequestDTO;
import org.apollo.api.dto.LoginResponseDTO;
import org.apollo.api.model.Roles;
import org.apollo.api.repository.AuthUserRepository;
import org.apollo.api.security.AuthUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldReturnBearerTokenWhenCredentialsAreValidForTenant() {
        LoginRequestDTO request = new LoginRequestDTO(10L, "admin@apollo.com", "password");
        AuthUser authUser = authenticatedUser();
        when(authUserRepository.findActiveByCompanyIdAndEmail(10L, "admin@apollo.com"))
                .thenReturn(List.of(authUser));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(org.mockito.ArgumentMatchers.any())).thenReturn("generated-token");

        LoginResponseDTO response = authService.login(request);

        assertEquals("generated-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void shouldRejectCredentialsFromAnotherTenant() {
        LoginRequestDTO request = new LoginRequestDTO(20L, "admin@apollo.com", "password");
        when(authUserRepository.findActiveByCompanyIdAndEmail(20L, "admin@apollo.com"))
                .thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Credenciais inválidas", exception.getReason());
    }

    private AuthUser authenticatedUser() {
        AuthUser user = mock(AuthUser.class);
        when(user.getUserId()).thenReturn(1L);
        when(user.getCompanyId()).thenReturn(10L);
        when(user.getUserType()).thenReturn("ADMINISTRATOR");
        when(user.getEmail()).thenReturn("admin@apollo.com");
        when(user.getPassword()).thenReturn("encoded-password");
        when(user.isActive()).thenReturn(true);
        when(user.getRole()).thenReturn(new Roles(1L, "ADMINISTRATOR", null));
        return user;
    }
}
