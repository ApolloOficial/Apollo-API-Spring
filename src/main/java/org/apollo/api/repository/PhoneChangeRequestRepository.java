package org.apollo.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apollo.api.dto.PhoneChangeRequestDTO;
import org.apollo.api.enums.PhoneChangeStatusEnum;
import org.apollo.api.model.PhoneChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhoneChangeRequestRepository extends JpaRepository<PhoneChangeRequest, UUID> {

    boolean existsByEmployeeIdAndStatus(UUID employeeId, PhoneChangeStatusEnum status);

    @Query("""
            SELECT new org.apollo.api.dto.PhoneChangeRequestDTO(r.id, r.employeeId, e.fullName, e.email,
                   r.newPhone, r.status, r.createdAt, r.reviewedAt)
            FROM PhoneChangeRequest r
            JOIN Employee e ON e.id = r.employeeId
            JOIN CompanyUnit cu ON cu.id = e.companyUnitId
            WHERE cu.company.id = :companyId
              AND cu.id = :unitId
              AND r.status = :status
            ORDER BY r.createdAt DESC
            """)
    List<PhoneChangeRequestDTO> findByScopeAndStatus(@Param("companyId") Long companyId,
                                                     @Param("unitId") UUID unitId,
                                                     @Param("status") PhoneChangeStatusEnum status);

    @Query("""
            SELECT r FROM PhoneChangeRequest r
            JOIN Employee e ON e.id = r.employeeId
            JOIN CompanyUnit cu ON cu.id = e.companyUnitId
            WHERE r.id = :id
              AND cu.company.id = :companyId
              AND cu.id = :unitId
            """)
    Optional<PhoneChangeRequest> findByIdInScope(@Param("id") UUID id,
                                                 @Param("companyId") Long companyId,
                                                 @Param("unitId") UUID unitId);
}
