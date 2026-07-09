package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.BatchDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Batch;
import org.apollo.api.repository.BatchRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchRepository batchRepository;

    public List<BatchDTO> findAll() {
        return batchRepository.findAll().stream().map(this::toDTO).toList();
    }

    public BatchDTO findById(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + id));
        return toDTO(batch);
    }

    public BatchDTO create(BatchDTO dto) {
        Batch batch = toEntity(dto);
        return toDTO(batchRepository.save(batch));
    }

    public BatchDTO update(Long id, BatchDTO dto) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado: " + id));

        batch.setBillNumber(dto.getBillNumber());
        batch.setManufacturer(dto.getManufacturer());
        batch.setModel(dto.getModel());
        batch.setAcquisitionDt(dto.getAcquisitionDt());
        batch.setPanelsQtt(dto.getPanelsQtt());

        return toDTO(batchRepository.save(batch));
    }

    public void delete(Long id) {
        if (!batchRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lote não encontrado: " + id);
        }
        batchRepository.deleteById(id);
    }

    private BatchDTO toDTO(Batch batch) {
        return new BatchDTO(
                batch.getId(),
                batch.getBillNumber(),
                batch.getManufacturer(),
                batch.getModel(),
                batch.getAcquisitionDt(),
                batch.getPanelsQtt()
        );
    }

    private Batch toEntity(BatchDTO dto) {
        return new Batch(
                null,
                dto.getBillNumber(),
                dto.getManufacturer(),
                dto.getModel(),
                dto.getAcquisitionDt(),
                dto.getPanelsQtt()
        );
    }
}