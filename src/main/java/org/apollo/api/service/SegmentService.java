package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SegmentDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Segment;
import org.apollo.api.repository.SegmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Segmentos de mercado: cadastrados no 1o ano, so leitura aqui.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SegmentService {

    private final SegmentRepository segmentRepository;

    public Page<SegmentDTO> findAll(Pageable pageable) {
        return segmentRepository.findAll(pageable).map(this::toDTO);
    }

    public SegmentDTO findById(Long id) {
        return toDTO(segmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Segment not found: " + id)));
    }

    private SegmentDTO toDTO(Segment segment) {
        return new SegmentDTO(segment.getId(), segment.getName(), segment.getDescription());
    }
}
