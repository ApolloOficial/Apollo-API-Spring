package org.apollo.api.repository.spec;

import jakarta.persistence.criteria.Subquery;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.model.Employee;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Standardized, null-safe dynamic filters for Employee.
 * <p>
 * Each method only ever returns a predicate when the corresponding filter value is
 * present; when a filter is not supplied we return {@code null} (a Specification that
 * matches everything), so no nullable bind parameter is ever sent to the database. This
 * is the pattern all "dynamic search" repositories in this project should follow instead
 * of the "(:param IS NULL OR ...)" JPQL anti-pattern.
 */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    /** Restricts results to employees whose company unit belongs to the given tenant. */
    public static Specification<Employee> belongsToCompany(Long companyId) {
        return (root, query, cb) -> {
            Subquery<UUID> unitIdsForCompany = query.subquery(UUID.class);
            var unitRoot = unitIdsForCompany.from(CompanyUnit.class);
            unitIdsForCompany.select(unitRoot.get("id"))
                    .where(cb.equal(unitRoot.get("company").get("id"), companyId));
            return root.get("companyUnitId").in(unitIdsForCompany);
        };
    }

    public static Specification<Employee> fullNameContains(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        String pattern = "%" + fullName.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("fullName")), pattern);
    }

    public static Specification<Employee> emailContains(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String pattern = "%" + email.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), pattern);
    }

    public static Specification<Employee> hasRoleId(Long roleId) {
        if (roleId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("roleId"), roleId);
    }

    public static Specification<Employee> isActive(Boolean active) {
        if (active == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("isActive"), active);
    }

    /** Restringe a uma filial especifica (o tenant continua sendo aplicado por belongsToCompany). */
    public static Specification<Employee> inCompanyUnit(UUID companyUnitId) {
        if (companyUnitId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("companyUnitId"), companyUnitId);
    }

    public static Specification<Employee> hasId(UUID id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }
}
