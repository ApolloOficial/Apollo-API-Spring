package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SuggestedInternalRelocationCreateDTO;
import org.apollo.api.dto.SuggestedInternalRelocationDTO;
import org.apollo.api.enums.RelocationStatusEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Employee;
import org.apollo.api.model.PanelString;
import org.apollo.api.model.SuggestedInternalRelocation;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.repository.PanelStringRepository;
import org.apollo.api.repository.SuggestedInternalRelocationRepository;
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
public class SuggestedInternalRelocationService {

    private final SuggestedInternalRelocationRepository relocationRepository;
    private final PanelStringRepository panelStringRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    /**
     * originUnitId = filial onde a string esta hoje; destinationUnitId = filial sugerida.
     * (Aba "Minha filial" / "Outras filiais" da tela de realocacoes.)
     */
    @Transactional(readOnly = true)
    public Page<SuggestedInternalRelocationDTO> findAll(RelocationStatusEnum status, UUID originUnitId,
                                                        UUID destinationUnitId, Pageable pageable) {
        Specification<SuggestedInternalRelocation> spec = Specification
                .where(Specs.<SuggestedInternalRelocation>equalTo(
                        r -> r.get("panelString").get("inverter").get("companyUnit").get("company").get("id"), companyId()))
                .and(Specs.<SuggestedInternalRelocation>equalTo(r -> r.get("status"), status))
                .and(Specs.<SuggestedInternalRelocation>equalTo(
                        r -> r.get("panelString").get("inverter").get("companyUnit").get("id"), originUnitId))
                .and(Specs.<SuggestedInternalRelocation>equalTo(r -> r.get("suggestedUnit").get("id"), destinationUnitId));
        return relocationRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public SuggestedInternalRelocationDTO findById(Long id) {
        return toDTO(findRelocation(id));
    }

    /** O operador sugere mover placas de uma string para outra filial da empresa. */
    public SuggestedInternalRelocationDTO create(SuggestedInternalRelocationCreateDTO dto) {
        PanelString string = panelStringRepository
                .findByIdAndInverterCompanyUnitCompanyId(dto.stringId(), companyId())
                .orElseThrow(() -> new ResourceNotFoundException("String not found: " + dto.stringId()));
        companyUnitRepository.findByIdAndCompanyId(dto.suggestedUnitId(), companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + dto.suggestedUnitId()));

        SuggestedInternalRelocation relocation = new SuggestedInternalRelocation();
        relocation.setPanelString(string);
        relocation.setQuantity(dto.quantity());
        relocation.setSuggestedUnit(companyUnitRepository.getReferenceById(dto.suggestedUnitId()));
        relocation.setRequestedBy(employeeRepository.getReferenceById(tenantContext.getUserId()));
        relocation.setJustification(dto.justification());
        relocation.setStatus(RelocationStatusEnum.PENDENTE);
        relocation.setSuggestedAt(DbProcedures.now());
        // saveAndFlush: o trigger do banco valida (mesma empresa, filial diferente, quantidade...)
        // e o erro precisa aparecer agora, nao so no commit.
        return toDTO(relocationRepository.saveAndFlush(relocation));
    }

    /** O gerente decide: APROVADA ou REJEITADA. */
    public SuggestedInternalRelocationDTO review(Long id, RelocationStatusEnum status) {
        if (status != RelocationStatusEnum.APROVADA && status != RelocationStatusEnum.REJEITADA) {
            throw new BusinessRuleException("Review status must be APROVADA or REJEITADA");
        }
        SuggestedInternalRelocation relocation = findRelocation(id);
        if (relocation.getStatus() != RelocationStatusEnum.PENDENTE) {
            throw new BusinessRuleException("Only a PENDENTE suggestion can be reviewed");
        }
        Employee reviewer = employeeRepository.getReferenceById(tenantContext.getUserId());
        relocation.setStatus(status);
        relocation.setReviewedBy(reviewer);
        relocation.setReviewedAt(DbProcedures.now());
        return toDTO(relocationRepository.saveAndFlush(relocation));
    }

    /** Marca como CONCLUIDA uma sugestao aprovada (a movimentacao fisica ja foi feita). */
    public SuggestedInternalRelocationDTO complete(Long id) {
        SuggestedInternalRelocation relocation = findRelocation(id);
        if (relocation.getStatus() != RelocationStatusEnum.APROVADA) {
            throw new BusinessRuleException("Only an APROVADA suggestion can be completed");
        }
        relocation.setStatus(RelocationStatusEnum.CONCLUIDA);
        return toDTO(relocationRepository.saveAndFlush(relocation));
    }

    private SuggestedInternalRelocation findRelocation(Long id) {
        return relocationRepository.findByIdAndPanelStringInverterCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Relocation not found: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private SuggestedInternalRelocationDTO toDTO(SuggestedInternalRelocation r) {
        PanelString string = r.getPanelString();
        var origin = string.getInverter().getCompanyUnit();
        Employee reviewer = r.getReviewedBy();
        return new SuggestedInternalRelocationDTO(r.getId(), string.getId(), string.getCode(),
                string.getInverter().getCode(), origin.getId(), origin.getName(), r.getQuantity(),
                r.getSuggestedUnit().getId(), r.getSuggestedUnit().getName(),
                r.getRequestedBy().getId(), r.getRequestedBy().getFullName(),
                reviewer != null ? reviewer.getId() : null, reviewer != null ? reviewer.getFullName() : null,
                r.getJustification(), r.getStatus(), r.getSuggestedAt(), r.getReviewedAt());
    }
}
