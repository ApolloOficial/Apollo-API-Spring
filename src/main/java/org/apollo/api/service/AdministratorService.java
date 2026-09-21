package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.AdministratorCreateDTO;
import org.apollo.api.dto.AdministratorDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Administrator;
import org.apollo.api.model.Company;
import org.apollo.api.model.Roles;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.repository.RolesRepository;
import org.apollo.api.repository.AdministratorRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdministratorService {

    private final AdministratorRepository administratorRepository;
    private final RolesRepository rolesRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantContext tenantContext;

    public List<AdministratorDTO> findAll() {
        return administratorRepository.findAllByCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    public AdministratorDTO findById(Long id) {
        return toDTO(findUser(id));
    }

    public AdministratorDTO create(AdministratorCreateDTO dto) {
        Long companyId = companyId();
        if (administratorRepository.findByCompanyIdAndEmail(companyId, dto.getEmail()).isPresent()) {
            throw new BusinessRuleException("Usuário com este email já existe nesta empresa");
        }
        if (administratorRepository.findByCompanyIdAndCpf(companyId, dto.getCpf()).isPresent()) {
            throw new BusinessRuleException("Usuário com este CPF já existe nesta empresa");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + companyId));
        Roles role = findRole(dto.getRoleId());

        Administrator administrator = new Administrator();
        administrator.setCompany(company);
        administrator.setRole(role);
        administrator.setFullName(dto.getFullName());
        administrator.setEmail(dto.getEmail());
        administrator.setCpf(dto.getCpf());
        administrator.setPassword(passwordEncoder.encode(dto.getPassword()));
        return toDTO(administratorRepository.save(administrator));
    }

    public AdministratorDTO update(Long id, AdministratorDTO dto) {
        Administrator administrator = findUser(id);
        Long companyId = companyId();
        administratorRepository.findByCompanyIdAndEmail(companyId, dto.getEmail())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new BusinessRuleException("Usuário com este email já existe nesta empresa");
                });
        administratorRepository.findByCompanyIdAndCpf(companyId, dto.getCpf())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new BusinessRuleException("Usuário com este CPF já existe nesta empresa");
                });

        administrator.setFullName(dto.getFullName());
        administrator.setEmail(dto.getEmail());
        administrator.setCpf(dto.getCpf());
        administrator.setRole(findRole(dto.getRoleId()));
        return toDTO(administratorRepository.save(administrator));
    }

    public void delete(Long id) {
        administratorRepository.delete(findUser(id));
    }

    private Administrator findUser(Long id) {
        return administratorRepository.findByIdAndCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    private Roles findRole(Long roleId) {
        return rolesRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado: " + roleId));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private AdministratorDTO toDTO(Administrator administrator) {
        return new AdministratorDTO(
                administrator.getId(),
                administrator.getCompany().getId(),
                administrator.getRole().getId(),
                administrator.getRole().getName(),
                administrator.getFullName(),
                administrator.getEmail(),
                administrator.getCpf()
        );
    }
}
