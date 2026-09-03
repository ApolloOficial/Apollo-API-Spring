package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.AdministratorCreateDTO;
import org.apollo.api.dto.UserDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Company;
import org.apollo.api.model.Roles;
import org.apollo.api.model.User;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.repository.RolesRepository;
import org.apollo.api.repository.UserRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantContext tenantContext;

    public List<UserDTO> findAll() {
        return userRepository.findAllByCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    public UserDTO findById(Long id) {
        return toDTO(findUser(id));
    }

    public UserDTO create(AdministratorCreateDTO dto) {
        Long companyId = companyId();
        if (userRepository.findByCompanyIdAndEmail(companyId, dto.getEmail()).isPresent()) {
            throw new BusinessRuleException("Usuário com este email já existe nesta empresa");
        }
        if (userRepository.findByCompanyIdAndCpf(companyId, dto.getCpf()).isPresent()) {
            throw new BusinessRuleException("Usuário com este CPF já existe nesta empresa");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + companyId));
        Roles role = findRole(dto.getRoleId());

        User user = new User();
        user.setCompany(company);
        user.setRole(role);
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setCpf(dto.getCpf());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return toDTO(userRepository.save(user));
    }

    public UserDTO update(Long id, UserDTO dto) {
        User user = findUser(id);
        Long companyId = companyId();
        userRepository.findByCompanyIdAndEmail(companyId, dto.getEmail())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new BusinessRuleException("Usuário com este email já existe nesta empresa");
                });
        userRepository.findByCompanyIdAndCpf(companyId, dto.getCpf())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new BusinessRuleException("Usuário com este CPF já existe nesta empresa");
                });

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setCpf(dto.getCpf());
        user.setRole(findRole(dto.getRoleId()));
        return toDTO(userRepository.save(user));
    }

    public void delete(Long id) {
        userRepository.delete(findUser(id));
    }

    private User findUser(Long id) {
        return userRepository.findByIdAndCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    private Roles findRole(Long roleId) {
        return rolesRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado: " + roleId));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getCompany().getId(),
                user.getRole().getId(),
                user.getRole().getName(),
                user.getFullName(),
                user.getEmail(),
                user.getCpf()
        );
    }
}
