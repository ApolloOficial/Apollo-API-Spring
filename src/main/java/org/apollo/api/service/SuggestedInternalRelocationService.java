package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SuggestedInternalRelocationDTO;
import org.apollo.api.enums.RelocationStatusEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.*;
import org.apollo.api.repository.*;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuggestedInternalRelocationService {

    private final SuggestedInternalRelocationRepository relocationRepository;
    private final BatchRepository batchRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    public List<SuggestedInternalRelocationDTO> findAll() {
        return relocationRepository.findAllByBatchCompanyUnitCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    public SuggestedInternalRelocationDTO create(SuggestedInternalRelocationDTO dto) {
        Batch batch = findBatch(dto.getBatchId());
        CompanyUnit destination = findUnit(dto.getSuggestedUnitId());
        Employee requester = employeeRepository.findByIdAndCompanyUnitCompanyId(dto.getRequestedById(), companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário solicitante não encontrado"));

        SuggestedInternalRelocation relocation = new SuggestedInternalRelocation();
        relocation.setBatch(batch);
        relocation.setSuggestedUnit(destination);
        relocation.setRequestedBy(requester);
        relocation.setQuantity(dto.getQuantity());
        relocation.setJustification(dto.getJustification());
        relocation.setStatus(RelocationStatusEnum.PENDENTE);
        return toDTO(relocationRepository.save(relocation));
    }

    public SuggestedInternalRelocationDTO review(Long id, RelocationStatusEnum status, UUID reviewerId) {
        SuggestedInternalRelocation relocation = findRelocation(id);
        if (relocation.getStatus() != RelocationStatusEnum.PENDENTE) {
            throw new BusinessRuleException("Apenas solicitações pendentes podem ser revisadas");
        }
        Employee reviewer = employeeRepository.findByIdAndCompanyUnitCompanyId(reviewerId, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Revisor não encontrado"));
        relocation.setStatus(status);
        relocation.setReviewedBy(reviewer);
        relocation.setReviewedAt(LocalDateTime.now());
        return toDTO(relocationRepository.save(relocation));
    }

    private SuggestedInternalRelocation findRelocation(Long id) {
        return relocationRepository.findByIdAndBatchCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Realocação não encontrada: " + id));
    }

    private Batch findBatch(UUID id) {
        return batchRepository.findByIdAndCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + id));
    }

    private CompanyUnit findUnit(UUID id) {
        return companyUnitRepository.findByIdAndCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade de destino não encontrada: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private SuggestedInternalRelocationDTO toDTO(SuggestedInternalRelocation r) {
        return new SuggestedInternalRelocationDTO(
                r.getId(), r.getBatch().getId(), r.getQuantity(), r.getSuggestedUnit().getId(),
                r.getRequestedBy().getId(),
                r.getReviewedBy() != null ? r.getReviewedBy().getId() : null,
                r.getJustification(), r.getStatus()
        );
    }
}