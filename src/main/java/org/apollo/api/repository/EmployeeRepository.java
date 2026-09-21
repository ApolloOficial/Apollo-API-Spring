package org.apollo.api.repository;

import org.apollo.api.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);
    List<Employee> findByIsActive(Boolean isActive);

    List<Employee> findByCompanyUnitId(UUID companyUnitId);

    List<Employee> findByRoleId(Long roleId);
}