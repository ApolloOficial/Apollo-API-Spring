package org.apollo.api.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.WarningDTO;
import org.apollo.api.enums.PriorityEnum;
import org.apollo.api.enums.WarningStatusEnum;
import org.apollo.api.enums.WarningTypeEnum;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Inverter;
import org.apollo.api.model.Panel;
import org.apollo.api.model.PanelString;
import org.apollo.api.model.Warning;
import org.apollo.api.repository.MaintenanceRegisterRepository;
import org.apollo.api.repository.WarningRepository;
import org.apollo.api.security.TenantContext;
import org.apollo.api.util.Scope;
import org.apollo.api.util.Specs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarningService {

    private final WarningRepository warningRepository;
    private final MaintenanceRegisterRepository maintenanceRegisterRepository;
    private final TenantContext tenantContext;

    public Page<WarningDTO> findAll(PriorityEnum severity, WarningTypeEnum type, WarningStatusEnum status,
                                    UUID companyUnitId, UUID stringId, Long panelId, Pageable pageable) {
        Specification<Warning> spec = Specification
                .where(inCompany(companyId()))
                .and(Specs.<Warning>equalTo(w -> w.get("severity"), severity))
                .and(Specs.<Warning>equalTo(w -> w.get("type"), type))
                .and(Specs.<Warning>equalTo(w -> w.get("status"), status))
                .and(companyUnitId == null ? null : inUnit(companyUnitId))
                .and(stringId == null ? null : forString(stringId))
                .and(Specs.<Warning>equalTo(w -> w.get("panel").get("id"), panelId));
        Page<Warning> page = warningRepository.findAll(spec, pageable);
        Map<Long, Long> maintenanceByWarning = maintenanceIds(page.getContent());
        return page.map(w -> toDTO(w, maintenanceByWarning.get(w.getId())));
    }

    public WarningDTO findById(Long id) {
        Specification<Warning> spec = Specification
                .where(inCompany(companyId()))
                .and(Specs.<Warning>equalTo(w -> w.get("id"), id));
        Warning warning = warningRepository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException("Warning not found: " + id));
        return toDTO(warning, maintenanceIds(List.of(warning)).get(warning.getId()));
    }

    private Specification<Warning> inCompany(Long companyId) {
        return (root, query, cb) -> Scope.warningInCompany(cb, root, companyId);
    }

    private Specification<Warning> inUnit(UUID unitId) {
        return (root, query, cb) -> Scope.warningInUnit(cb, root, unitId);
    }

    // O alerta e da string (alerta de string) ou de uma placa que pertence a essa string.
    private Specification<Warning> forString(UUID stringId) {
        return (root, query, cb) -> {
            Join<Object, Object> panelString = root.join("panel", JoinType.LEFT).join("panelString", JoinType.LEFT);
            return cb.or(
                    cb.equal(root.get("panelString").get("id"), stringId),
                    cb.equal(panelString.get("id"), stringId));
        };
    }

    private Map<Long, Long> maintenanceIds(List<Warning> warnings) {
        Map<Long, Long> result = new HashMap<>();
        if (warnings.isEmpty()) {
            return result;
        }
        List<Long> ids = warnings.stream().map(Warning::getId).toList();
        for (Object[] row : maintenanceRegisterRepository.findIdsByWarningIds(ids)) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    private WarningDTO toDTO(Warning w, Long maintenanceId) {
        Panel panel = w.getPanel();
        PanelString string = w.getPanelString() != null ? w.getPanelString()
                : (panel != null ? panel.getPanelString() : null);
        Inverter inverter = string != null ? string.getInverter() : null;
        return new WarningDTO(w.getId(),
                string != null ? string.getId() : null,
                string != null ? string.getCode() : null,
                inverter != null ? inverter.getCode() : null,
                panel != null ? panel.getId() : null,
                panel != null ? panel.getSerialNumber() : null,
                inverter != null ? inverter.getCompanyUnit().getId() : null,
                w.getType(), w.getSeverity(), w.getStatus(), w.getMessage(), w.getReportedBy(),
                w.getGenerationDt(), w.getResolvedAt(), maintenanceId);
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }
}
