package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.LoginRequestDTO;
import org.apollo.api.dto.LoginResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciais inválidas";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request == null
                || !StringUtils.hasText(request.getEmail())
                || !StringUtils.hasText(request.getPassword())) {
            throw invalidCredentials();
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
                throw invalidCredentials();
            }

            String token = jwtService.generateToken(userDetails);

            return new LoginResponseDTO(token, "Bearer");
        } catch (AuthenticationException exception) {
            throw invalidCredentials();
        }
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                INVALID_CREDENTIALS_MESSAGE
        );
    }
}