package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.BatchDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Batch;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.repository.BatchRepository;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BatchService {
    private final BatchRepository batchRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public List<BatchDTO> findAll() { return batchRepository.findAllByCompanyUnitCompanyId(companyId()).stream().map(this::toDTO).toList(); }
    @Transactional(readOnly = true)
    public BatchDTO findById(UUID id) { return toDTO(findBatch(id)); }

    public BatchDTO create(BatchDTO dto) {
        CompanyUnit unit = findUnit(dto.getCompanyUnitId());
        validateBillNumber(unit.getId(), dto.getBillNumber(), null);
        Batch batch = new Batch();
        batch.setCompanyUnit(unit);
        updateFields(batch, dto);
        return toDTO(batchRepository.save(batch));
    }

    public BatchDTO update(UUID id, BatchDTO dto) {
        Batch batch = findBatch(id);
        CompanyUnit unit = findUnit(dto.getCompanyUnitId());
        validateBillNumber(unit.getId(), dto.getBillNumber(), id);
        batch.setCompanyUnit(unit);
        updateFields(batch, dto);
        return toDTO(batchRepository.save(batch));
    }

    public void delete(UUID id) { batchRepository.delete(findBatch(id)); }

    private void validateBillNumber(UUID unitId, String billNumber, UUID batchId) {
        boolean exists = batchId == null
                ? batchRepository.existsByCompanyUnitIdAndBillNumber(unitId, billNumber)
                : batchRepository.existsByCompanyUnitIdAndBillNumberAndIdNot(unitId, billNumber, batchId);
        if (exists) throw new BusinessRuleException("Já existe lote com esta nota fiscal nesta unidade");
    }

    private Batch findBatch(UUID id) {
        return batchRepository.findByIdAndCompanyUnitCompanyId(id, companyId()).orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + id));
    }

    private CompanyUnit findUnit(UUID id) {
        return companyUnitRepository.findByIdAndCompanyId(id, companyId()).orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private void updateFields(Batch batch, BatchDTO dto) {
        batch.setBillNumber(dto.getBillNumber()); batch.setManufacturer(dto.getManufacturer()); batch.setModel(dto.getModel()); batch.setAcquisitionDt(dto.getAcquisitionDt()); batch.setPanelsQtt(dto.getPanelsQtt()); batch.setUnitCost(dto.getUnitCost());
    }

    private BatchDTO toDTO(Batch batch) {
        return new BatchDTO(batch.getId(), batch.getCompanyUnit().getId(), batch.getBillNumber(), batch.getManufacturer(), batch.getModel(), batch.getAcquisitionDt(), batch.getPanelsQtt(), batch.getUnitCost(), batch.getCreatedAt());
    }
}
