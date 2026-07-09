package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.PanelDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Batch;
import org.apollo.api.model.Panel;
import org.apollo.api.repository.BatchRepository;
import org.apollo.api.repository.PanelRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PanelService {

    private final PanelRepository panelRepository;
    private final BatchRepository batchRepository;

    public List<PanelDTO> findAll() {
        return panelRepository.findAll().stream().map(this::toDTO).toList();
    }

    public PanelDTO findById(Long id) {
        Panel panel = panelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Painel não encontrado: " + id));
        return toDTO(panel);
    }

    public PanelDTO create(PanelDTO dto) {
        Batch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + dto.getBatchId()));

        Panel panel = toEntity(dto, batch);
        return toDTO(panelRepository.save(panel));
    }

    public PanelDTO update(Long id, PanelDTO dto) {
        Panel panel = panelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Painel não encontrado: " + id));

        Batch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + dto.getBatchId()));

        panel.setBatch(batch);
        panel.setCoUnityId(dto.getCoUnityId());
        panel.setEstimatedLifeCycle(dto.getEstimatedLifeCycle());
        panel.setSerialNumber(dto.getSerialNumber());
        panel.setBarcode(dto.getBarcode());
        panel.setOperatingStatsEnum(dto.getOperatingStatsEnum());
        panel.setRatedEfficiency(dto.getRatedEfficiency());
        panel.setInstallationDt(dto.getInstallationDt());

        return toDTO(panelRepository.save(panel));
    }

    public void delete(Long id) {
        if (!panelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Painel não encontrado: " + id);
        }
        panelRepository.deleteById(id);
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

    private Panel toEntity(PanelDTO dto, Batch batch) {
        return new Panel(
                null,
                batch,
                dto.getCoUnityId(),
                dto.getEstimatedLifeCycle(),
                dto.getSerialNumber(),
                dto.getBarcode(),
                dto.getOperatingStatsEnum(),
                dto.getRatedEfficiency(),
                dto.getInstallationDt()
        );
    }
}