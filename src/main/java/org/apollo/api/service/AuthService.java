package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.LoginRequestDTO;
import org.apollo.api.dto.LoginResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciais inválidas";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword())
            );
        if (request == null || !StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getPassword())) {
            throw invalidCredentials();
        }

            if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
                throw new AuthenticationServiceException("Principal autenticado inválido");
            }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw invalidCredentials();
        }

            return new LoginResponseDTO(jwtTokenProvider.generateToken(userDetails), "Bearer");
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE);
        }
        return new LoginResponseDTO(jwtService.generateToken(user), "Bearer");
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE);
    }
}
