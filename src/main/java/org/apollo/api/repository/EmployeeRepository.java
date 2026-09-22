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
            WHERE e.id = :id
                AND cu.company.id = :companyId
    """)
    Optional<Employee> findByIdAndCompanyUnitCompanyId(@Param("id") UUID id, @Param("companyId") Long companyId);
    Optional<Employee> findByEmail(String email);
}