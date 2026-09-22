package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.MaintenanceRegisterDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Batch;
import org.apollo.api.model.Employee;
import org.apollo.api.model.MaintenanceRegister;
import org.apollo.api.model.MaintenanceType;
import org.apollo.api.repository.BatchRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.repository.MaintenanceRegisterRepository;
import org.apollo.api.repository.MaintenanceTypeRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceRegisterService {
    private final MaintenanceRegisterRepository maintenanceRegisterRepository;
    private final MaintenanceTypeRepository maintenanceTypeRepository;
    private final BatchRepository batchRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public List<MaintenanceRegisterDTO> findAll() {
        return maintenanceRegisterRepository.findAllByBatchCompanyUnitCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceRegisterDTO findById(Long id) {
        return toDTO(findRegister(id));
    }

    public MaintenanceRegisterDTO create(MaintenanceRegisterDTO dto) {
        MaintenanceRegister register = new MaintenanceRegister();
        register.setCreatedBy(currentEmployee());
        applyRelations(register, dto);
        applyFields(register, dto);
        return toDTO(maintenanceRegisterRepository.save(register));
    }

    public MaintenanceRegisterDTO update(Long id, MaintenanceRegisterDTO dto) {
        MaintenanceRegister register = findRegister(id); applyRelations(register, dto); applyFields(register, dto); return toDTO(maintenanceRegisterRepository.save(register));
    }

    public void delete(Long id) {
        maintenanceRegisterRepository.delete(findRegister(id));
    }

    private void applyRelations(MaintenanceRegister register, MaintenanceRegisterDTO dto) {
        MaintenanceType type = maintenanceTypeRepository.findById(dto.getMaintenanceTypeId()).orElseThrow(() -> new ResourceNotFoundException("Tipo de manutenção não encontrado: " + dto.getMaintenanceTypeId()));
        Batch batch = batchRepository.findByIdAndCompanyUnitCompanyId(dto.getBatchId(), companyId()).orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + dto.getBatchId()));
        Employee technician = employeeRepository.findByIdAndCompanyUnitCompanyId(dto.getTechnicianId(), companyId()).orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado: " + dto.getTechnicianId()));
        register.setMaintenanceType(type); register.setBatch(batch); register.setTechnician(technician);
        if (dto.getParentMaintenanceId() == null) { register.setParentMaintenance(null); return; }
        MaintenanceRegister parent = findRegister(dto.getParentMaintenanceId());
        if (parent.getId().equals(register.getId())) throw new BusinessRuleException("Uma manutenção não pode ser pai de si mesma");
        register.setParentMaintenance(parent);
    }

    private void applyFields(MaintenanceRegister r, MaintenanceRegisterDTO dto) {
        r.setTechnicalReport(dto.getTechnicalReport()); r.setMaintenanceStatus(dto.getMaintenanceStatus()); r.setPriority(dto.getPriority()); r.setOpeningDt(dto.getOpeningDt()); r.setDueDate(dto.getDueDate()); r.setConcludedAt(dto.getConcludedAt()); r.setEstimatedCost(dto.getEstimatedCost()); r.setActualCost(dto.getActualCost());
    }

    private Employee currentEmployee() {
        if (!"EMPLOYEE".equals(tenantContext.getUserType())) throw new AccessDeniedException("Somente funcionários podem abrir manutenção");
        try { return employeeRepository.findByIdAndCompanyUnitCompanyId(UUID.fromString(tenantContext.getUserId()), companyId()).orElseThrow(() -> new AccessDeniedException("Funcionário autenticado não pertence à empresa")); }
        catch (IllegalArgumentException ex) { throw new AccessDeniedException("Identidade de funcionário inválida"); }
    }

    private MaintenanceRegister findRegister(Long id) {
        return maintenanceRegisterRepository.findByIdAndBatchCompanyUnitCompanyId(id, companyId()).orElseThrow(() -> new ResourceNotFoundException("Manutenção não encontrada: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private MaintenanceRegisterDTO toDTO(MaintenanceRegister r) {
        return new MaintenanceRegisterDTO(r.getId(), r.getParentMaintenance() == null ? null : r.getParentMaintenance().getId(), r.getMaintenanceType().getId(), r.getBatch().getId(), r.getTechnician().getId(), r.getCreatedBy().getId(), r.getTechnicalReport(), r.getMaintenanceStatus(), r.getPriority(), r.getOpeningDt(), r.getDueDate(), r.getConcludedAt(), r.getEstimatedCost(), r.getActualCost());
    }
}
