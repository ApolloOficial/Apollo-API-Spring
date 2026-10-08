package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SuggestedExternalRelocationCreateDTO;
import org.apollo.api.dto.SuggestedExternalRelocationDTO;
import org.apollo.api.enums.RelocationStatusEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Employee;
import org.apollo.api.model.Panel;
import org.apollo.api.model.SuggestedExternalRelocation;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.repository.PanelRepository;
import org.apollo.api.repository.SegmentRepository;
import org.apollo.api.repository.SuggestedExternalRelocationRepository;
import org.apollo.api.security.TenantContext;
import org.apollo.api.util.DbProcedures;
import org.apollo.api.util.Specs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SuggestedExternalRelocationService {

    private final SuggestedExternalRelocationRepository relocationRepository;
    private final PanelRepository panelRepository;
    private final CompanyRepository companyRepository;
    private final SegmentRepository segmentRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public Page<SuggestedExternalRelocationDTO> findAll(RelocationStatusEnum status, UUID originUnitId,
                                                        Long destinationCompanyId, Pageable pageable) {
        Specification<SuggestedExternalRelocation> spec = Specification
                .where(Specs.<SuggestedExternalRelocation>equalTo(
                        r -> r.get("panel").get("panelString").get("inverter").get("companyUnit").get("company").get("id"),
                        companyId()))
                .and(Specs.<SuggestedExternalRelocation>equalTo(r -> r.get("status"), status))
                .and(Specs.<SuggestedExternalRelocation>equalTo(
                        r -> r.get("panel").get("panelString").get("inverter").get("companyUnit").get("id"), originUnitId))
                .and(Specs.<SuggestedExternalRelocation>equalTo(r -> r.get("destinationCompany").get("id"), destinationCompanyId));
        return relocationRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public SuggestedExternalRelocationDTO findById(Long id) {
        return toDTO(findRelocation(id));
    }

    /** O operador sugere enviar uma placa para outra empresa. */
    public SuggestedExternalRelocationDTO create(SuggestedExternalRelocationCreateDTO dto) {
        Panel panel = panelRepository.findByIdAndPanelStringInverterCompanyUnitCompanyId(dto.panelId(), companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Panel not found: " + dto.panelId()));
        if (!companyRepository.existsById(dto.destinationCompanyId())) {
            throw new ResourceNotFoundException("Company not found: " + dto.destinationCompanyId());
        }
        if (!segmentRepository.existsById(dto.segmentId())) {
            throw new ResourceNotFoundException("Segment not found: " + dto.segmentId());
        }

        SuggestedExternalRelocation relocation = new SuggestedExternalRelocation();
        relocation.setPanel(panel);
        relocation.setDestinationCompany(companyRepository.getReferenceById(dto.destinationCompanyId()));
        relocation.setSegment(segmentRepository.getReferenceById(dto.segmentId()));
        relocation.setRequestedBy(employeeRepository.getReferenceById(tenantContext.getUserId()));
        relocation.setJustification(dto.justification());
        relocation.setStatus(RelocationStatusEnum.PENDENTE);
        relocation.setSuggestedAt(DbProcedures.now());
        return toDTO(relocationRepository.saveAndFlush(relocation));
    }

    /** O gerente decide: APROVADA ou REJEITADA. */
    public SuggestedExternalRelocationDTO review(Long id, RelocationStatusEnum status) {
        if (status != RelocationStatusEnum.APROVADA && status != RelocationStatusEnum.REJEITADA) {
            throw new BusinessRuleException("Review status must be APROVADA or REJEITADA");
        }
        SuggestedExternalRelocation relocation = findRelocation(id);
        if (relocation.getStatus() != RelocationStatusEnum.PENDENTE) {
            throw new BusinessRuleException("Only a PENDENTE suggestion can be reviewed");
        }
        Employee reviewer = employeeRepository.getReferenceById(tenantContext.getUserId());
        relocation.setStatus(status);
        relocation.setReviewedBy(reviewer);
        relocation.setReviewedAt(DbProcedures.now());
        return toDTO(relocationRepository.saveAndFlush(relocation));
    }

    public SuggestedExternalRelocationDTO complete(Long id) {
        SuggestedExternalRelocation relocation = findRelocation(id);
        if (relocation.getStatus() != RelocationStatusEnum.APROVADA) {
            throw new BusinessRuleException("Only an APROVADA suggestion can be completed");
        }
        relocation.setStatus(RelocationStatusEnum.CONCLUIDA);
        return toDTO(relocationRepository.saveAndFlush(relocation));
    }

    private SuggestedExternalRelocation findRelocation(Long id) {
        return relocationRepository.findByIdAndPanelPanelStringInverterCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Relocation not found: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private SuggestedExternalRelocationDTO toDTO(SuggestedExternalRelocation r) {
        var origin = r.getPanel().getPanelString().getInverter().getCompanyUnit();
        Employee reviewer = r.getReviewedBy();
        return new SuggestedExternalRelocationDTO(r.getId(), r.getPanel().getId(), r.getPanel().getSerialNumber(),
                origin.getId(), origin.getName(), r.getDestinationCompany().getId(),
                r.getDestinationCompany().getName(), r.getSegment().getId(), r.getSegment().getName(),
                r.getRequestedBy().getId(), r.getRequestedBy().getFullName(),
                reviewer != null ? reviewer.getId() : null, reviewer != null ? reviewer.getFullName() : null,
                r.getJustification(), r.getStatus(), r.getSuggestedAt(), r.getReviewedAt());
    }
}
