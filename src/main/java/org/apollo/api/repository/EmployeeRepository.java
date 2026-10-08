package org.apollo.api.repository;

import org.apollo.api.dto.SearchResultDTO;
import org.apollo.api.model.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BUG FIX (findAll with a "role" or "email" filter throwing
 * "org.postgresql.util.PSQLException: ERROR: function lower(bytea) does not exist"):
 * <p>
 * The previous version of this repository used a single hand-written JPQL query with
 * the classic "(:param IS NULL OR LOWER(field) = LOWER(:param))" pattern to make every
 * filter optional. With Hibernate 6/7 + PostgreSQL, when a nullable String parameter is
 * bound directly as the sole argument of a function (e.g. LOWER(:role)) with no other
 * literal context to anchor its type, Hibernate can fail to infer a concrete JDBC type
 * for the null value and falls back to binding it as VARBINARY, which Postgres receives
 * as "bytea". Postgres then rejects lower(bytea) because that overload does not exist -
 * exactly the reported error.
 * <p>
 * The fix is architectural, not a one-line patch: dynamic/optional filters must never be
 * expressed as "IS NULL OR ..." bind parameters in JPQL. We now use JPA Specifications
 * (see org.apollo.api.repository.spec.EmployeeSpecifications), which simply omit a
 * predicate entirely when a filter is not provided - so no null value for email/role is
 * ever sent to Postgres in the first place. This also gives us free, safe composition
 * with org.springframework.data.domain.Pageable.
 */
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);

    // Kept as a plain derived query: both parameters are always required (never null)
    // at every call site, so this does not hit the nullable-parameter bug described
    // above. Used by AuthService, MaintenanceRegisterService, CompanyUnitService, etc.
    // Employee.companyUnitId is a plain UUID column (no @ManyToOne), so Spring Data cannot
    // derive a "CompanyUnitCompanyId" path from the method name (that was the startup
    // failure: "No property 'companyUnitCompanyId' found for type 'Employee'"). An explicit
    // entity join expresses the tenant check instead. Both parameters are always non-null.
    @Query("""
            SELECT e FROM Employee e
            JOIN CompanyUnit cu ON cu.id = e.companyUnitId
            WHERE e.id = :id AND cu.company.id = :companyId
            """)
    Optional<Employee> findByIdAndCompanyUnitCompanyId(@Param("id") UUID id, @Param("companyId") Long companyId);

    // Standardized dynamic-search JPQL (replaces the old EntityManager/StringBuilder
    // based SearchRepositoryImpl). ":search" is always non-blank by the time this runs
    // (SearchService normalizes/validates it first), so there is no nullable-parameter
    // risk here - see the class-level Javadoc above for why that matters.
    @Query("""
            SELECT new org.apollo.api.dto.SearchResultDTO(e.id, e.fullName, 'Employee')
            FROM Employee e
            JOIN CompanyUnit cu ON cu.id = e.companyUnitId
            WHERE cu.company.id = :companyId
              AND LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY e.fullName ASC
            """)
    List<SearchResultDTO> search(@Param("search") String search, @Param("companyId") Long companyId, Pageable pageable);
}
