package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.EmployeeCreateDTO;
import org.apollo.api.dto.EmployeeDTO;
import org.apollo.api.dto.EmployeeUpdateDTO;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public List<EmployeeDTO> findAll(String email, String role, Boolean isActive) {
        List<Employee> employees = employeeRepository.findAllByCompanyUnitCompanyIdAndFilters(
                companyId(), blankToNull(email), blankToNull(role), isActive);
        Map<Long, String> roleNames = roleNamesFor(employees);
        return employees.stream().map(e -> toDTO(e, roleNames.get(e.getRoleId()))).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDTO findById(UUID id) {
        Employee employee = findEmployee(id);
        return toDTO(employee, roleName(employee.getRoleId()));
    }

    public EmployeeDTO create(EmployeeCreateDTO dto) {
        Roles role = findRole(dto.getRoleId());
        tenantContext.requireCanAssign(role.getName());
        CompanyUnit unit = dto.getCompanyUnitId() != null ? findUnit(dto.getCompanyUnitId()) : null;

        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setRoleId(role.getId());
        employee.setCompanyUnitId(unit != null ? unit.getId() : null);
        employee.setIsActive(dto.getActive() != null ? dto.getActive() : true);
        employee.setPasswordHash(passwordEncoder.encode(dto.getTemporaryPassword()));
        employee.setCreatedAt(LocalDateTime.now());
        return toDTO(employeeRepository.save(employee), role.getName());
    }

    public EmployeeDTO update(UUID id, EmployeeUpdateDTO dto) {
        Employee employee = findEmployee(id);
        Roles role = findRole(dto.getRoleId());
        tenantContext.requireCanAssign(role.getName());
        CompanyUnit unit = dto.getCompanyUnitId() != null ? findUnit(dto.getCompanyUnitId()) : null;

        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setRoleId(role.getId());
        employee.setCompanyUnitId(unit != null ? unit.getId() : null);
        employee.setIsActive(dto.getActive());
        return toDTO(employeeRepository.save(employee), role.getName());
    }

    public EmployeeDTO deactivate(UUID id) {
        Employee employee = findEmployee(id);
        if (Boolean.FALSE.equals(employee.getIsActive())) {
            throw new BusinessRuleException("Funcionário já está inativo");
        }
        employee.setIsActive(false);
        return toDTO(employeeRepository.save(employee), roleName(employee.getRoleId()));
    }

    public void delete(UUID id) {
        employeeRepository.delete(findEmployee(id));
    }

    private Roles findRole(Long roleId) {
        return rolesRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo não encontrado: " + roleId));
    }

    private CompanyUnit findUnit(UUID unitId) {
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

    private String roleName(Long roleId) {
        return rolesRepository.findById(roleId).map(Roles::getName).orElse(null);
    }

    private Map<Long, String> roleNamesFor(List<Employee> employees) {
        List<Long> roleIds = employees.stream().map(Employee::getRoleId).distinct().toList();
        return rolesRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(Roles::getId, Roles::getName));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private EmployeeDTO toDTO(Employee e, String roleName) {
        return new EmployeeDTO(e.getId(), e.getFullName(), e.getEmail(), roleName, e.getCompanyUnitId(), e.getIsActive());
    }
}