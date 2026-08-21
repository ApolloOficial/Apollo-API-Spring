package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.LoginRequestDTO;
import org.apollo.api.dto.LoginResponseDTO;
import org.apollo.api.model.User;
import org.apollo.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciais inválidas";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getPassword())) {
            throw invalidCredentials();
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw invalidCredentials();
        }

        return new LoginResponseDTO(jwtService.generateToken(user), "Bearer");
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE);
    }
}
