package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.WarningDTO;
import org.apollo.api.enums.WarningStatusEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Panel;
import org.apollo.api.model.Warning;
import org.apollo.api.repository.PanelRepository;
import org.apollo.api.repository.WarningRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarningService {

    private final WarningRepository warningRepository;
    private final PanelRepository panelRepository;
    private final TenantContext tenantContext;

    public List<WarningDTO> findAll() {
        return warningRepository.findAllByPanelBatchCompanyUnitCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    public WarningDTO findById(Long id) {
        return toDTO(findWarning(id));
    }

    public WarningDTO create(WarningDTO dto) {
        Panel panel = findPanel(dto.getPanelId());
        Warning warning = new Warning();
        warning.setPanel(panel);
        applyFields(warning, dto);
        return toDTO(warningRepository.save(warning));
    }

    public WarningDTO resolve(Long id) {
        Warning warning = findWarning(id);
        warning.setStatus(WarningStatusEnum.RESOLVIDO);
        warning.setResolvedAt(LocalDateTime.now());
        return toDTO(warningRepository.save(warning));
    }

    public void delete(Long id) {
        warningRepository.delete(findWarning(id));
    }

    private void applyFields(Warning warning, WarningDTO dto) {
        warning.setType(dto.getType());
        warning.setSeverity(dto.getSeverity());
        warning.setMessage(dto.getMessage());
        if (dto.getStatus() == WarningStatusEnum.RESOLVIDO) {
            throw new BusinessRuleException("Use o endpoint de resolução para marcar como resolvido");
        }
        warning.setStatus(dto.getStatus() != null ? dto.getStatus() : WarningStatusEnum.ATIVO);
    }

    private Warning findWarning(Long id) {
        return warningRepository.findByIdAndPanelBatchCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado: " + id));
    }

    private Panel findPanel(Long id) {
        return panelRepository.findByIdAndBatchCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Painel não encontrado: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private WarningDTO toDTO(Warning w) {
        return new WarningDTO(w.getId(), w.getPanel().getId(), w.getType(), w.getSeverity(), w.getStatus(), w.getMessage());
    }
}