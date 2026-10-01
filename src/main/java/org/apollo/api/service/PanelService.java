package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.PanelActivateDTO;
import org.apollo.api.dto.PanelDTO;
import org.apollo.api.dto.PanelDeactivateDTO;
import org.apollo.api.dto.PanelIssueDTO;
import org.apollo.api.dto.WarningDTO;
import org.apollo.api.enums.OperatingStatsEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Panel;
import org.apollo.api.model.PanelOverview;
import org.apollo.api.repository.PanelOverviewRepository;
import org.apollo.api.repository.PanelRepository;
import org.apollo.api.security.TenantContext;
import org.apollo.api.util.DbProcedures;
import org.apollo.api.util.Scope;
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
public class PanelService {

    private final PanelOverviewRepository panelOverviewRepository;
    private final PanelRepository panelRepository;
    private final WarningService warningService;
    private final DbProcedures procedures;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public Page<PanelDTO> findAll(UUID stringId, UUID companyUnitId, OperatingStatsEnum status,
                                  String serialNumber, String barcode, Pageable pageable) {
        Specification<PanelOverview> spec = Specification
                .where(Scope.<PanelOverview>unitColumnInCompany("companyUnitId", companyId()))
                .and(Specs.<PanelOverview>equalTo(p -> p.get("stringId"), stringId))
                .and(Specs.<PanelOverview>equalTo(p -> p.get("companyUnitId"), companyUnitId))
                .and(Specs.<PanelOverview>equalTo(p -> p.get("operatingStats"), status))
                .and(Specs.<PanelOverview>contains(p -> p.get("serialNumber"), serialNumber))
                .and(Specs.<PanelOverview>contains(p -> p.get("barcode"), barcode));
        return panelOverviewRepository.findAll(spec, pageable).map(PanelDTO::of);
    }

    @Transactional(readOnly = true)
    public PanelDTO findById(Long id) {
        Specification<PanelOverview> spec = Specification
                .where(Scope.<PanelOverview>unitColumnInCompany("companyUnitId", companyId()))
                .and(Specs.<PanelOverview>equalTo(p -> p.get("panelId"), id));
        return panelOverviewRepository.findOne(spec)
                .map(PanelDTO::of)
                .orElseThrow(() -> new ResourceNotFoundException("Panel not found: " + id));
    }

    @Transactional(readOnly = true)
    public PanelDTO findByBarcode(String barcode) {
        Specification<PanelOverview> spec = Specification
                .where(Scope.<PanelOverview>unitColumnInCompany("companyUnitId", companyId()))
                .and(Specs.<PanelOverview>equalTo(p -> p.get("barcode"), barcode == null ? null : barcode.trim()));
        return panelOverviewRepository.findOne(spec)
                .map(PanelDTO::of)
                .orElseThrow(() -> new ResourceNotFoundException("Panel not found for barcode: " + barcode));
    }

    /** Tecnico ativa a placa pelo bipe. Sem data de instalacao vale hoje. */
    public PanelDTO activate(PanelActivateDTO dto) {
        Panel panel = requirePanelOfMyUnit(dto.barcode());
        procedures.activatePanel(panel.getBarcode(), tenantContext.getUserId(),
                dto.installationDt() != null ? dto.installationDt() : DbProcedures.today());
        return findById(panel.getId());
    }

    /** Tecnico desativa a placa (ex.: microfissura, garantia). */
    public PanelDTO deactivate(PanelDeactivateDTO dto) {
        Panel panel = requirePanelOfMyUnit(dto.barcode());
        procedures.deactivatePanel(panel.getBarcode(), tenantContext.getUserId(), dto.reason());
        return findById(panel.getId());
    }

    /** Relato de campo do tecnico: gera um alerta de placa. */
    public WarningDTO reportIssue(PanelIssueDTO dto) {
        switch (dto.type()) {
            case DANO_FISICO, SUJIDADE, PONTO_QUENTE, CONEXAO -> { }
            default -> throw new BusinessRuleException(
                    "Type must be DANO_FISICO, SUJIDADE, PONTO_QUENTE or CONEXAO for a panel report");
        }
        Panel panel = requirePanelOfMyUnit(dto.barcode());
        Long warningId = procedures.reportPanelIssue(panel.getBarcode(), tenantContext.getUserId(),
                dto.type().name(), dto.severity().name(), dto.message());
        return warningService.findById(warningId);
    }

    /** A placa precisa existir na empresa e na filial do tecnico (o banco confere o resto). */
    private Panel requirePanelOfMyUnit(String barcode) {
        String code = barcode == null ? "" : barcode.trim();
        Panel panel = panelRepository.findByBarcodeAndPanelStringInverterCompanyUnitCompanyId(code, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Panel not found for barcode: " + code));
        UUID panelUnit = panel.getPanelString().getInverter().getCompanyUnit().getId();
        if (!panelUnit.equals(tenantContext.getCompanyUnitId())) {
            throw new BusinessRuleException("The panel " + panel.getSerialNumber() + " belongs to another unit");
        }
        return panel;
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }
}
