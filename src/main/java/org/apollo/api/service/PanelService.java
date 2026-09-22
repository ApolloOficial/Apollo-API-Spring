package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.PanelDTO;
import org.apollo.api.enums.OperatingStatsEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Batch;
import org.apollo.api.model.Panel;
import org.apollo.api.repository.BatchRepository;
import org.apollo.api.repository.PanelRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PanelService {
    private final PanelRepository panelRepository;
    private final BatchRepository batchRepository;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public List<PanelDTO> findAll() {
        return panelRepository.findAllByBatchCompanyUnitCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PanelDTO findById(Long id) {
        return toDTO(findPanel(id));
    }

    public PanelDTO create(PanelDTO dto) {
        Batch batch = findBatch(dto.getBatchId()); validateInstallation(dto); Panel panel = new Panel(); panel.setBatch(batch); updateFields(panel, dto); return toDTO(panelRepository.save(panel)); }

    public PanelDTO update(Long id, PanelDTO dto) {
        Panel panel = findPanel(id); panel.setBatch(findBatch(dto.getBatchId())); validateInstallation(dto); updateFields(panel, dto); return toDTO(panelRepository.save(panel)); }

    public void delete(Long id) {
        panelRepository.delete(findPanel(id));
    }

    private Panel findPanel(Long id) {
        return panelRepository.findByIdAndBatchCompanyUnitCompanyId(id, companyId()).orElseThrow(() -> new ResourceNotFoundException("Painel não encontrado: " + id));
    }

    private Batch findBatch(UUID id) {
        return batchRepository.findByIdAndCompanyUnitCompanyId(id, companyId()).orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + id));
    }

    private void validateInstallation(PanelDTO dto) {
        boolean stock = dto.getOperatingStats() == OperatingStatsEnum.EM_ESTOQUE;

        if (stock && dto.getInstallationDt() != null)
            throw new BusinessRuleException("Painel em estoque não pode ter data de instalação");

        if (!stock && dto.getInstallationDt() == null)
                throw new BusinessRuleException("Data de instalação é obrigatória para painel fora de estoque");
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private void updateFields(Panel panel, PanelDTO dto) {
        panel.setEstimatedLifeCycle(dto.getEstimatedLifeCycle()); panel.setSerialNumber(dto.getSerialNumber()); panel.setBarcode(dto.getBarcode()); panel.setOperatingStats(dto.getOperatingStats()); panel.setRatedEfficiency(dto.getRatedEfficiency()); panel.setInstallationDt(dto.getInstallationDt());
    }

    private PanelDTO toDTO(Panel panel) {
        return new PanelDTO(panel.getId(), panel.getBatch().getId(), panel.getEstimatedLifeCycle(), panel.getSerialNumber(), panel.getBarcode(), panel.getOperatingStats(), panel.getRatedEfficiency(), panel.getInstallationDt());
    }
}
