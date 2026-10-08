package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.MaintenanceTypeDTO;
import org.apollo.api.repository.MaintenanceTypeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceTypeService {

    private final MaintenanceTypeRepository maintenanceTypeRepository;

    public List<MaintenanceTypeDTO> findAll() {
        return maintenanceTypeRepository.findAll(Sort.by("name")).stream()
                .map(t -> new MaintenanceTypeDTO(t.getId(), t.getName(), t.getDescription(), t.getDefaultIntervalDays()))
                .toList();
    }
}
