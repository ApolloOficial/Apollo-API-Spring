package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.EmployeeCreateDTO;
import org.apollo.api.dto.EmployeeDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.model.Employee;
import org.apollo.api.model.Roles;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.repository.RolesRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RolesRepository rolesRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public List<EmployeeDTO> findAll() {
        return employeeRepository.findAllByCompanyUnitCompanyId(companyId()).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDTO findById(UUID id) {
        return toDTO(findEmployee(id));
    }

    public EmployeeDTO create(EmployeeCreateDTO dto) {
        Roles role = findRole(dto.getRoleId());
        tenantContext.requireCanAssign(role.getName());
        CompanyUnit unit = findUnit(dto.getCompanyUnitId());

        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setRoleId(role.getId());
        employee.setCompanyUnitId(unit.getId());
        employee.setIsActive(dto.getActive());
        employee.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        employee.setCreatedAt(LocalDateTime.now());
        return toDTO(employeeRepository.save(employee));
    }

    public EmployeeDTO update(UUID id, EmployeeDTO dto) {
        Employee employee = findEmployee(id);
        Roles role = findRole(dto.getRoleId());
        tenantContext.requireCanAssign(role.getName());
        findUnit(dto.getCompanyUnitId());

        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setRoleId(role.getId());
        employee.setCompanyUnitId(dto.getCompanyUnitId());
        employee.setIsActive(dto.getActive());
        return toDTO(employeeRepository.save(employee));
    }

    public void delete(UUID id) {
        employeeRepository.delete(findEmployee(id));
    }

    private Roles findRole(Long roleId) {
        return rolesRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo não encontrado: " + roleId));
    }

    private CompanyUnit findUnit(UUID unitId) {
        if (unitId == null) {
            throw new BusinessRuleException("Unidade é obrigatória para o funcionário");
        }
        return companyUnitRepository.findByIdAndCompanyId(unitId, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada: " + unitId));
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findByIdAndCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private EmployeeDTO toDTO(Employee e) {
        return new EmployeeDTO(e.getId(), e.getFullName(), e.getEmail(), e.getRoleId(), e.getCompanyUnitId(), e.getIsActive());
    }
}