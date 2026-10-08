package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.InverterModelDTO;
import org.apollo.api.dto.PanelModelDTO;
import org.apollo.api.dto.PartDTO;
import org.apollo.api.repository.InverterModelRepository;
import org.apollo.api.repository.PanelModelRepository;
import org.apollo.api.repository.PartRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Catalogos globais (nao pertencem a uma empresa): pecas, modelos de placa e de inversor.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final PartRepository partRepository;
    private final PanelModelRepository panelModelRepository;
    private final InverterModelRepository inverterModelRepository;

    public Page<PartDTO> parts(Pageable pageable) {
        return partRepository.findAll(pageable)
                .map(p -> new PartDTO(p.getId(), p.getSku(), p.getName(), p.getManufacturer()));
    }

    public Page<PanelModelDTO> panelModels(Pageable pageable) {
        return panelModelRepository.findAll(pageable)
                .map(m -> new PanelModelDTO(m.getId(), m.getManufacturer(), m.getModel(), m.getPmaxW(),
                        m.getEfficiencyPct(), m.getWarrantyYears()));
    }

    public Page<InverterModelDTO> inverterModels(Pageable pageable) {
        return inverterModelRepository.findAll(pageable)
                .map(m -> new InverterModelDTO(m.getId(), m.getBrand(), m.getModel(), m.getRatedPowerKw(),
                        m.getMpptCount(), m.getStringsPerMppt(), m.isStringCurrentMonitoring()));
    }
}
