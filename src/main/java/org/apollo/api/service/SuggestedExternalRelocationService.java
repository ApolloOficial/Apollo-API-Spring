package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SuggestedExternalRelocationDTO;
import org.apollo.api.enums.RelocationStatusEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.*;
import org.apollo.api.repository.*;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuggestedExternalRelocationService {

    private final SuggestedExternalRelocationRepository relocationRepository;
    private final PanelRepository panelRepository;
    private final CompanyRepository companyRepository;
    private final SegmentRepository segmentRepository;
    private final TenantContext tenantContext;

    public List<SuggestedExternalRelocationDTO> findAll() {
        return relocationRepository.findAllByPanelBatchCompanyUnitCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    public SuggestedExternalRelocationDTO create(SuggestedExternalRelocationDTO dto) {
        Panel panel = panelRepository.findByIdAndBatchCompanyUnitCompanyId(dto.getPanelId(), companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Painel não encontrado: " + dto.getPanelId()));
        Company destination = companyRepository.findById(dto.getDestinationCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa de destino não encontrada"));
        Segment segment = segmentRepository.findById(dto.getSegmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Segmento não encontrado"));

        SuggestedExternalRelocation relocation = new SuggestedExternalRelocation();
        relocation.setPanel(panel);
        relocation.setDestinationCompany(destination);
        relocation.setSegment(segment);
        relocation.setJustification(dto.getJustification());
        relocation.setStatus(RelocationStatusEnum.PENDENTE);
        return toDTO(relocationRepository.save(relocation));
    }

    public SuggestedExternalRelocationDTO review(Long id, RelocationStatusEnum status) {
        SuggestedExternalRelocation relocation = findRelocation(id);
        if (relocation.getStatus() != RelocationStatusEnum.PENDENTE) {
            throw new BusinessRuleException("Apenas solicitações pendentes podem ser revisadas");
        }
        relocation.setStatus(status);
        relocation.setReviewedAt(LocalDateTime.now());
        return toDTO(relocationRepository.save(relocation));
    }

    private SuggestedExternalRelocation findRelocation(Long id) {
        return relocationRepository.findByIdAndPanelBatchCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Realocação não encontrada: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private SuggestedExternalRelocationDTO toDTO(SuggestedExternalRelocation r) {
        return new SuggestedExternalRelocationDTO(
                r.getId(), r.getPanel().getId(), r.getDestinationCompany().getId(),
                r.getSegment().getId(), r.getJustification(), r.getStatus()
        );
    }
}