package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.BatchDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Batch;
import org.apollo.api.model.Company;
import org.apollo.api.repository.BatchRepository;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchRepository batchRepository;
    private final CompanyRepository companyRepository;
    private final TenantContext tenantContext;

    public List<BatchDTO> findAll() {
        return batchRepository.findAllByCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    public BatchDTO findById(Long id) {
        return toDTO(findBatch(id));
    }

    public BatchDTO create(BatchDTO dto) {
        Long companyId = companyId();
        if (batchRepository.existsByCompanyIdAndBillNumber(companyId, dto.getBillNumber())) {
            throw new BusinessRuleException("Já existe lote com esta nota fiscal nesta empresa");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + companyId));
        Batch batch = new Batch();
        batch.setCompany(company);
        updateFields(batch, dto);
        return toDTO(batchRepository.save(batch));
    }

    public BatchDTO update(Long id, BatchDTO dto) {
        Batch batch = findBatch(id);
        if (batchRepository.existsByCompanyIdAndBillNumberAndIdNot(companyId(), dto.getBillNumber(), id)) {
            throw new BusinessRuleException("Já existe lote com esta nota fiscal nesta empresa");
        }
        updateFields(batch, dto);
        return toDTO(batchRepository.save(batch));
    }

    public void delete(Long id) {
        batchRepository.delete(findBatch(id));
    }

    private Batch findBatch(Long id) {
        return batchRepository.findByIdAndCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private void updateFields(Batch batch, BatchDTO dto) {
        batch.setBillNumber(dto.getBillNumber());
        batch.setManufacturer(dto.getManufacturer());
        batch.setModel(dto.getModel());
        batch.setAcquisitionDt(dto.getAcquisitionDt());
        batch.setPanelsQtt(dto.getPanelsQtt());
    }

    private BatchDTO toDTO(Batch batch) {
        return new BatchDTO(
                batch.getId(),
                batch.getCompany().getId(),
                batch.getBillNumber(),
                batch.getManufacturer(),
                batch.getModel(),
                batch.getAcquisitionDt(),
                batch.getPanelsQtt()
        );
    }
}
