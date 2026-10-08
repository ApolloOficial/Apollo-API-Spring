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
import org.apollo.api.repository.spec.EmployeeSpecifications;
import org.apollo.api.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    /**
     * BUG FIX (missing pagination): findAll used to return every employee of the
     * tenant in a single unbounded List. It now accepts a Pageable and returns a
     * Page<EmployeeDTO>, exactly like the other list endpoints in this project.
     * <p>
     * BUG FIX ("function lower(bytea) does not exist" when filtering by role/email):
     * filters are now built with JPA Specifications (see EmployeeSpecifications),
     * which only add a predicate when a value is actually provided. No nullable
     * parameter is ever bound into a LOWER(...) call, which is what caused Hibernate
     * to mis-infer the parameter's JDBC type as bytea.
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> findAll(String fullName, String email, String role, Boolean isActive, UUID companyUnitId, Pageable pageable) {
        Long roleId = resolveRoleIdOrNull(role);
        if (role != null && !role.isBlank() && roleId == null) {
            // Cargo informado nao existe (nem por ID nem por nome): nenhum funcionario
            // pode casar, devolve pagina vazia sem mandar filtro ambiguo ao banco.
            return Page.empty(pageable);
        }

        Specification<Employee> spec = Specification
                .where(EmployeeSpecifications.belongsToCompany(companyId()))
                .and(EmployeeSpecifications.fullNameContains(fullName))
                .and(EmployeeSpecifications.emailContains(email))
                .and(EmployeeSpecifications.hasRoleId(roleId))
                .and(EmployeeSpecifications.isActive(isActive))
                .and(EmployeeSpecifications.inCompanyUnit(companyUnitId));

        Page<Employee> employees = employeeRepository.findAll(spec, pageable);
        Map<Long, String> roleNames = roleNamesFor(employees.getContent());
        return employees.map(e -> toDTO(e, roleNames.get(e.getRoleId())));
    }

    @Transactional(readOnly = true)
    public EmployeeDTO findById(UUID id) {
        Employee employee = findEmployee(id);
        return toDTO(employee, roleName(employee.getRoleId()));
    }

    public EmployeeDTO create(EmployeeCreateDTO dto) {
        Roles role = findRole(dto.getRoleId());
        requireNotManagerRole(role);
        tenantContext.requireCanAssign(role.getName());
        CompanyUnit unit = findUnit(dto.getCompanyUnitId());
        requireEmailAvailable(dto.getEmail(), null);
        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setRoleId(role.getId());
        employee.setCompanyUnitId(unit.getId());
        employee.setIsActive(dto.getActive() != null ? dto.getActive() : true);
        employee.setPasswordHash(passwordEncoder.encode(dto.getTemporaryPassword()));
        // O banco exige quem cadastrou (gerente ativo da mesma filial) para analista/operador/tecnico.
        employee.setCreatedBy(tenantContext.getUserId());
        employee.setCreatedAt(LocalDateTime.now());
        return toDTO(employeeRepository.saveAndFlush(employee), role.getName());
    }

    public EmployeeDTO update(UUID id, EmployeeUpdateDTO dto) {
        Employee employee = findEmployee(id);
        Roles role = findRole(dto.getRoleId());
        tenantContext.requireCanAssign(role.getName());
        CompanyUnit unit = dto.getCompanyUnitId() != null ? findUnit(dto.getCompanyUnitId()) : null;
        requireEmailAvailable(dto.getEmail(), id);

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
            throw new BusinessRuleException("Employee is already inactive");
        }
        employee.setIsActive(false);
        return toDTO(employeeRepository.save(employee), roleName(employee.getRoleId()));
    }

    public void delete(UUID id) {
        Employee employee = findEmployee(id);
        try {
            employeeRepository.delete(employee);
            // flush forca o DELETE agora, dentro do try, para a violacao de FK ser
            // capturada aqui (senao so estouraria no commit, fora deste metodo).
            employeeRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Employee has linked records (service orders, relocations or unit responsibility) "
                            + "and cannot be deleted; deactivate it instead");
        }
    }

    private void requireNotManagerRole(Roles role) {
        String name = role.getName() == null ? "" : role.getName().trim().toUpperCase();
        if (name.equals("GERENTE") || name.equals("MANAGER")) {
            throw new BusinessRuleException("The manager role is registered by the administrator, not through this endpoint");
        }
    }

    private Roles findRole(Long roleId) {
        return rolesRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
    }

    private CompanyUnit findUnit(UUID unitId) {
        return companyUnitRepository.findByIdAndCompanyId(unitId, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findByIdAndCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    /**
     * BUG FIX (PUT failing on a duplicate email): employee.email has a *global*
     * UNIQUE constraint in the database (not scoped per tenant). Previously the
     * service saved directly and let the raw DataIntegrityViolationException bubble
     * up as a generic 409 with no useful detail. We now check proactively and raise
     * a clear, actionable 400 Bad Request instead.
     */
    private void requireEmailAvailable(String email, UUID currentEmployeeId) {
        employeeRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(currentEmployeeId)) {
                throw new BusinessRuleException("A user with this email already exists: " + email);
            }
        });
    }

    private Long resolveRoleIdOrNull(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String value = role.trim();
        // Aceita o ID do cargo ("2") ou o nome ("MANAGER"/"Gerente").
        if (value.length() <= 18 && value.chars().allMatch(Character::isDigit)) {
            Long id = Long.valueOf(value);
            return rolesRepository.existsById(id) ? id : null;
        }
        return rolesRepository.findByNameIgnoreCase(value).map(Roles::getId).orElse(null);
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private String roleName(Long roleId) {
        return rolesRepository.findById(roleId).map(Roles::getName).orElse(null);
    }

    private Map<Long, String> roleNamesFor(List<Employee> employees) {
        List<Long> roleIds = employees.stream().map(Employee::getRoleId).distinct().toList();
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        return rolesRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(Roles::getId, Roles::getName));
    }

    private EmployeeDTO toDTO(Employee e, String roleName) {
        return new EmployeeDTO(e.getId(), e.getFullName(), e.getEmail(), roleName, e.getCompanyUnitId(), e.getIsActive());
    }
}
