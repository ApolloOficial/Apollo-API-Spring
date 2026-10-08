package org.apollo.api.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.MaintenanceCancelDTO;
import org.apollo.api.dto.MaintenanceChainDTO;
import org.apollo.api.dto.MaintenanceCompleteDTO;
import org.apollo.api.dto.MaintenanceCreateDTO;
import org.apollo.api.dto.MaintenancePartCreateDTO;
import org.apollo.api.dto.MaintenancePartDTO;
import org.apollo.api.dto.MaintenanceRegisterDTO;
import org.apollo.api.enums.MaintenanceStatusEnum;
import org.apollo.api.enums.PriorityEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Inverter;
import org.apollo.api.model.MaintenancePart;
import org.apollo.api.model.MaintenanceRegister;
import org.apollo.api.model.Panel;
import org.apollo.api.model.PanelString;
import org.apollo.api.model.Warning;
import org.apollo.api.repository.MaintenancePartRepository;
import org.apollo.api.repository.MaintenanceRegisterRepository;
import org.apollo.api.repository.PartRepository;
import org.apollo.api.security.TenantContext;
import org.apollo.api.util.DbProcedures;
import org.apollo.api.util.Scope;
import org.apollo.api.util.Specs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ordens de servico. Abrir, iniciar, concluir e cancelar passam pelas procedures do banco
 * (pr_open_maintenance, pr_start_maintenance, pr_complete_maintenance, pr_cancel_maintenance),
 * que tambem atualizam o status do alerta, da string e das placas.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceRegisterService {

    private final MaintenanceRegisterRepository maintenanceRegisterRepository;
    private final MaintenancePartRepository maintenancePartRepository;
    private final PartRepository partRepository;
    private final WarningService warningService;
    private final EntityManager entityManager;
    private final DbProcedures procedures;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public Page<MaintenanceRegisterDTO> findAll(Long id, UUID technicianId, Long maintenanceTypeId,
                                                MaintenanceStatusEnum status, PriorityEnum priority,
                                                Boolean thisMonth, Boolean overdue, UUID companyUnitId,
                                                Pageable pageable) {
        Specification<MaintenanceRegister> spec = Specification
                .where(inCompany(companyId()))
                .and(Specs.<MaintenanceRegister>equalTo(m -> m.get("id"), id))
                .and(Specs.<MaintenanceRegister>equalTo(m -> m.get("technician").get("id"), technicianId))
                .and(Specs.<MaintenanceRegister>equalTo(m -> m.get("maintenanceType").get("id"), maintenanceTypeId))
                .and(Specs.<MaintenanceRegister>equalTo(m -> m.get("maintenanceStatus"), status))
                .and(priority == null ? null : withSeverity(priority))
                .and(companyUnitId == null ? null : inUnit(companyUnitId))
                .and(Boolean.TRUE.equals(thisMonth) ? openedThisMonth() : null)
                .and(Boolean.TRUE.equals(overdue) ? isOverdue() : null);
        Page<MaintenanceRegister> page = maintenanceRegisterRepository.findAll(spec, pageable);
        Map<Long, BigDecimal> partsCost = partsCostFor(page.getContent());
        return page.map(m -> toDTO(m, partsCost.getOrDefault(m.getId(), BigDecimal.ZERO)));
    }

    @Transactional(readOnly = true)
    public MaintenanceRegisterDTO findById(Long id) {
        return toDTO(findRegister(id));
    }

    /**
     * Cadeia completa da OS: sobe pelos pais ate a raiz e desce por todos os retrabalhos.
     * A raiz tem chainLevel 1; path vai da raiz ate a OS. O escopo da empresa e checado na OS pedida.
     */
    @Transactional(readOnly = true)
    public List<MaintenanceChainDTO> chain(Long id) {
        MaintenanceRegister root = findRegister(id);
        java.util.Set<Long> seen = new java.util.HashSet<>();
        seen.add(root.getId());
        while (root.getParentMaintenance() != null && seen.add(root.getParentMaintenance().getId())) {
            root = maintenanceRegisterRepository.findById(root.getParentMaintenance().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service order not found: " + id));
        }

        List<MaintenanceChainDTO> chain = new java.util.ArrayList<>();
        Map<Long, List<Long>> paths = new HashMap<>();
        paths.put(root.getId(), List.of(root.getId()));
        chain.add(new MaintenanceChainDTO(root.getId(), null, root.getMaintenanceStatus(), root.getOpeningDt(),
                1, paths.get(root.getId())));

        List<Long> level = List.of(root.getId());
        java.util.Set<Long> visited = new java.util.HashSet<>(level);
        while (!level.isEmpty()) {
            List<Long> next = new java.util.ArrayList<>();
            for (MaintenanceRegister child : maintenanceRegisterRepository.findChildrenOf(level)) {
                if (!visited.add(child.getId())) {
                    continue;
                }
                Long parentId = child.getParentMaintenance().getId();
                List<Long> path = new java.util.ArrayList<>(paths.get(parentId));
                path.add(child.getId());
                paths.put(child.getId(), path);
                chain.add(new MaintenanceChainDTO(child.getId(), parentId, child.getMaintenanceStatus(),
                        child.getOpeningDt(), path.size(), path));
                next.add(child.getId());
            }
            level = next;
        }
        return chain;
    }

    /** O operador abre a OS a partir de um alerta ATIVO e escolhe o tecnico. */
    public MaintenanceRegisterDTO create(MaintenanceCreateDTO dto) {
        warningService.findById(dto.warningId()); // 404 se o alerta nao for da empresa do usuario
        Long id = procedures.openMaintenance(dto.warningId(), tenantContext.getUserId(), dto.technicianId(),
                dto.maintenanceTypeId(), dto.dueDate(), dto.estimatedCost(), dto.parentMaintenanceId());
        return reload(id);
    }

    /** Tecnico responsavel inicia a OS. */
    public MaintenanceRegisterDTO start(Long id) {
        MaintenanceRegister register = findRegister(id);
        requireAssignedTechnician(register);
        procedures.startMaintenance(id, tenantContext.getUserId());
        return reload(id);
    }

    /** Tecnico responsavel conclui a OS com o laudo tecnico. */
    public MaintenanceRegisterDTO complete(Long id, MaintenanceCompleteDTO dto) {
        MaintenanceRegister register = findRegister(id);
        requireAssignedTechnician(register);
        procedures.completeMaintenance(id, tenantContext.getUserId(), dto.technicalReport(), dto.laborCost());
        return reload(id);
    }

    /** Operador cancela a OS informando o motivo. */
    public MaintenanceRegisterDTO cancel(Long id, MaintenanceCancelDTO dto) {
        findRegister(id);
        procedures.cancelMaintenance(id, tenantContext.getUserId(), dto.reason());
        return reload(id);
    }

    @Transactional(readOnly = true)
    public List<MaintenancePartDTO> parts(Long id) {
        findRegister(id);
        return maintenancePartRepository.findByMaintenanceId(id).stream().map(this::toPartDTO).toList();
    }

    /** Lanca uma peca na OS; o banco baixa o estoque da filial (e valida status e saldo). */
    public List<MaintenancePartDTO> addPart(Long id, MaintenancePartCreateDTO dto) {
        MaintenanceRegister register = findRegister(id);
        if (tenantContext.getRoleName().equals("TECHNICIAN")) {
            requireAssignedTechnician(register);
        }
        if (!partRepository.existsById(dto.partId())) {
            throw new ResourceNotFoundException("Part not found: " + dto.partId());
        }
        if (maintenancePartRepository.findByMaintenanceId(id).stream()
                .anyMatch(p -> p.getId().getPartId().equals(dto.partId()))) {
            throw new BusinessRuleException("This part is already listed in the service order");
        }
        procedures.addMaintenancePart(id, dto.partId(), dto.quantity());
        return maintenancePartRepository.findByMaintenanceId(id).stream().map(this::toPartDTO).toList();
    }

    // ------------------------------------------------------------------ helpers

    // As procedures alteram linhas direto no banco: descarrega e limpa o cache da sessao
    // antes de reler, senao a resposta mostraria o estado antigo da OS/alerta/placas.
    private MaintenanceRegisterDTO reload(Long id) {
        entityManager.flush();
        entityManager.clear();
        return toDTO(findRegister(id));
    }

    private MaintenanceRegister findRegister(Long id) {
        Specification<MaintenanceRegister> spec = Specification
                .where(inCompany(companyId()))
                .and(Specs.<MaintenanceRegister>equalTo(m -> m.get("id"), id));
        return maintenanceRegisterRepository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException("Service order not found: " + id));
    }

    private void requireAssignedTechnician(MaintenanceRegister register) {
        if (!register.getTechnician().getId().equals(tenantContext.getUserId())) {
            throw new BusinessRuleException("This service order is assigned to another technician");
        }
    }

    private Specification<MaintenanceRegister> inCompany(Long companyId) {
        return (root, query, cb) -> Scope.warningInCompany(cb, root.join("warning"), companyId);
    }

    private Specification<MaintenanceRegister> inUnit(UUID unitId) {
        return (root, query, cb) -> Scope.warningInUnit(cb, root.join("warning"), unitId);
    }

    private Specification<MaintenanceRegister> withSeverity(PriorityEnum severity) {
        return (root, query, cb) -> cb.equal(root.join("warning").get("severity"), severity);
    }

    private Specification<MaintenanceRegister> openedThisMonth() {
        LocalDateTime start = DbProcedures.today().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.<LocalDateTime>get("openingDt"), start),
                cb.lessThan(root.<LocalDateTime>get("openingDt"), end));
    }

    private Specification<MaintenanceRegister> isOverdue() {
        LocalDateTime now = DbProcedures.now();
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("dueDate")),
                cb.lessThan(root.<LocalDateTime>get("dueDate"), now),
                root.get("maintenanceStatus").in(MaintenanceStatusEnum.ABERTA, MaintenanceStatusEnum.EM_ANDAMENTO));
    }

    private Map<Long, BigDecimal> partsCostFor(List<MaintenanceRegister> registers) {
        Map<Long, BigDecimal> totals = new HashMap<>();
        if (registers.isEmpty()) {
            return totals;
        }
        List<Long> ids = registers.stream().map(MaintenanceRegister::getId).toList();
        for (MaintenancePart part : maintenancePartRepository.findByMaintenanceIds(ids)) {
            BigDecimal line = lineCost(part);
            totals.merge(part.getId().getMaintenanceId(), line, BigDecimal::add);
        }
        return totals;
    }

    private BigDecimal lineCost(MaintenancePart part) {
        BigDecimal unit = part.getUnitCost() != null ? part.getUnitCost() : BigDecimal.ZERO;
        return unit.multiply(BigDecimal.valueOf(part.getQuantity()));
    }

    private MaintenanceRegisterDTO toDTO(MaintenanceRegister m) {
        BigDecimal partsCost = maintenancePartRepository.findByMaintenanceIds(List.of(m.getId())).stream()
                .map(this::lineCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        return toDTO(m, partsCost);
    }

    private MaintenanceRegisterDTO toDTO(MaintenanceRegister m, BigDecimal partsCost) {
        Warning warning = m.getWarning();
        Panel panel = warning.getPanel();
        PanelString string = warning.getPanelString() != null ? warning.getPanelString()
                : (panel != null ? panel.getPanelString() : null);
        Inverter inverter = string != null ? string.getInverter() : null;
        boolean overdue = m.getDueDate() != null && m.getDueDate().isBefore(DbProcedures.now())
                && (m.getMaintenanceStatus() == MaintenanceStatusEnum.ABERTA
                || m.getMaintenanceStatus() == MaintenanceStatusEnum.EM_ANDAMENTO);
        BigDecimal labor = m.getLaborCost() != null ? m.getLaborCost() : BigDecimal.ZERO;
        return new MaintenanceRegisterDTO(m.getId(),
                m.getParentMaintenance() != null ? m.getParentMaintenance().getId() : null,
                warning.getId(), warning.getType(), warning.getSeverity(),
                inverter != null ? inverter.getCompanyUnit().getId() : null,
                m.getMaintenanceType().getId(), m.getMaintenanceType().getName(),
                m.getTechnician().getId(), m.getTechnician().getFullName(), m.getCreatedBy().getId(),
                m.getTechnicalReport(), m.getMaintenanceStatus(), overdue, m.getOpeningDt(), m.getDueDate(),
                m.getConcludedAt(), m.getCancelledAt(), m.getCancellationReason(), m.getEstimatedCost(),
                m.getLaborCost(), partsCost, labor.add(partsCost));
    }

    private MaintenancePartDTO toPartDTO(MaintenancePart p) {
        return new MaintenancePartDTO(p.getPart().getId(), p.getPart().getSku(), p.getPart().getName(),
                p.getQuantity(), p.getUnitCost(), lineCost(p), p.getConsumedAt());
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }
}
