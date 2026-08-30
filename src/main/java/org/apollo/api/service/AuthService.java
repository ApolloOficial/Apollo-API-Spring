package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.LoginRequestDTO;
import org.apollo.api.dto.LoginResponseDTO;
import org.apollo.api.repository.AuthUserRepository;
import org.apollo.api.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciais inválidas";

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        var matchingUsers = authUserRepository
                .findActiveByCompanyIdAndEmail(request.getCompanyId(), request.getEmail())
                .stream()
                .filter(authUser -> passwordEncoder.matches(request.getPassword(), authUser.getPassword()))
                .map(AuthenticatedUser::new)
                .toList();

        if (matchingUsers.size() != 1) {
            throw invalidCredentials();
        }
        return new LoginResponseDTO(jwtService.generateToken(matchingUsers.getFirst()), "Bearer");
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE);
    }
}
