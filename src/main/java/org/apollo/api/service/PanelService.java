package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.PanelDTO;
import org.apollo.api.enums.OperatingStatsEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Batch;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.model.Panel;
import org.apollo.api.repository.BatchRepository;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.PanelRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PanelService {

    private final PanelRepository panelRepository;
    private final BatchRepository batchRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final TenantContext tenantContext;

    public List<PanelDTO> findAll() {
        return panelRepository.findAllByBatchCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    public PanelDTO findById(Long id) {
        return toDTO(findPanel(id));
    }

    public PanelDTO create(PanelDTO dto) {
        Batch batch = findBatch(dto.getBatchId());
        CompanyUnit unit = findUnit(dto.getCoUnityId());
        validateInstallation(dto);

        Panel panel = new Panel();
        panel.setBatch(batch);
        panel.setCoUnityId(unit.getId());
        updateFields(panel, dto);
        return toDTO(panelRepository.save(panel));
    }

    public PanelDTO update(Long id, PanelDTO dto) {
        Panel panel = findPanel(id);
        Batch batch = findBatch(dto.getBatchId());
        CompanyUnit unit = findUnit(dto.getCoUnityId());
        validateInstallation(dto);

        panel.setBatch(batch);
        panel.setCoUnityId(unit.getId());
        updateFields(panel, dto);
        return toDTO(panelRepository.save(panel));
    }

    public void delete(Long id) {
        panelRepository.delete(findPanel(id));
    }

    private Panel findPanel(Long id) {
        return panelRepository.findByIdAndBatchCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Painel não encontrado: " + id));
    }

    private Batch findBatch(Long id) {
        return batchRepository.findByIdAndCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + id));
    }

    private CompanyUnit findUnit(Long id) {
        return companyUnitRepository.findByIdAndCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada: " + id));
    }

    private void validateInstallation(PanelDTO dto) {
        boolean isStock = dto.getOperatingStatsEnum() == OperatingStatsEnum.EM_ESTOQUE;
        if (isStock && dto.getInstallationDt() != null) {
            throw new BusinessRuleException("Painel em estoque não pode ter data de instalação");
        }
        if (!isStock && dto.getInstallationDt() == null) {
            throw new BusinessRuleException("Data de instalação é obrigatória para painel fora de estoque");
        }
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private void updateFields(Panel panel, PanelDTO dto) {
        panel.setEstimatedLifeCycle(dto.getEstimatedLifeCycle());
        panel.setSerialNumber(dto.getSerialNumber());
        panel.setBarcode(dto.getBarcode());
        panel.setOperatingStatsEnum(dto.getOperatingStatsEnum());
        panel.setRatedEfficiency(dto.getRatedEfficiency());
        panel.setInstallationDt(dto.getInstallationDt());
    }

    private PanelDTO toDTO(Panel panel) {
        return new PanelDTO(
                panel.getId(),
                panel.getBatch().getId(),
                panel.getCoUnityId(),
                panel.getEstimatedLifeCycle(),
                panel.getSerialNumber(),
                panel.getBarcode(),
                panel.getOperatingStatsEnum(),
                panel.getRatedEfficiency(),
                panel.getInstallationDt()
        );
    }
}
