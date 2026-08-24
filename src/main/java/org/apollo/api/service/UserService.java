package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.UserDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Roles;
import org.apollo.api.model.User;
import org.apollo.api.repository.RolesRepository;
import org.apollo.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        return toDTO(user);
    }

    public UserDTO create(UserDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Usuário com este email já existe: " + dto.getEmail());
        }
        if (userRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new IllegalArgumentException("Usuário com este CPF já existe: " + dto.getCpf());
        }

        Roles role = rolesRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado: " + dto.getRoleId()));

        User user = toEntity(dto, role);
        user.setPassword(passwordEncoder.encode("changeme"));
        return toDTO(userRepository.save(user));
    }

    public UserDTO update(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Usuário com este email já existe: " + dto.getEmail());
        }
        if (!user.getCpf().equals(dto.getCpf()) && userRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new IllegalArgumentException("Usuário com este CPF já existe: " + dto.getCpf());
        }

        Roles role = rolesRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado: " + dto.getRoleId()));

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setCpf(dto.getCpf());
        user.setRole(role);

        return toDTO(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getRole().getId(),
                user.getRole().getName(),
                user.getFullName(),
                user.getEmail(),
                user.getCpf()
        );
    }

    private User toEntity(UserDTO dto, Roles role) {
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setCpf(dto.getCpf());
        user.setRole(role);
        return user;
    }
}
