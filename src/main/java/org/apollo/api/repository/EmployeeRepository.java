package org.apollo.api.repository;

import org.apollo.api.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Query("""
            SELECT e FROM Employee e
            JOIN CompanyUnit cu ON cu.id = e.companyUnitId
            WHERE cu.company.id = :companyId
            """)
    List<Employee> findAllByCompanyUnitCompanyId(@Param("companyId") Long companyId);

    @Query("""
            SELECT e FROM Employee e
            JOIN CompanyUnit cu ON cu.id = e.companyUnitId
            JOIN Roles r ON r.id = e.roleId
            WHERE cu.company.id = :companyId
              AND (:email IS NULL OR LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%')))
              AND (:role IS NULL OR LOWER(r.name) = LOWER(:role))
              AND (:isActive IS NULL OR e.isActive = :isActive)
            ORDER BY e.fullName ASC
            """)
    List<Employee> findAllByCompanyUnitCompanyIdAndFilters(
            @Param("companyId") Long companyId,
            @Param("email") String email,
            @Param("role") String role,
            @Param("isActive") Boolean isActive
    );

    @Query("""
            SELECT e FROM Employee e
            JOIN CompanyUnit cu ON cu.id = e.companyUnitId
            WHERE e.id = :id
              AND cu.company.id = :companyId
            """)
    Optional<Employee> findByIdAndCompanyUnitCompanyId(@Param("id") UUID id, @Param("companyId") Long companyId);

    Optional<Employee> findByEmail(String email);
}