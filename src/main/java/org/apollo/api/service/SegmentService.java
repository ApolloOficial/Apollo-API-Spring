package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.SegmentDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Segment;
import org.apollo.api.repository.SegmentRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;

    public List<SegmentDTO> findAll() {
        return segmentRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public SegmentDTO findById(Long id) {
        Segment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Segmento não encontrado: " + id));
        return toDTO(segment);
    }

    public SegmentDTO create(SegmentDTO dto) {
        if (segmentRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Segmento com este nome já existe: " + dto.getName());
        }
        Segment segment = toEntity(dto);
        return toDTO(segmentRepository.save(segment));
    }

    public SegmentDTO update(Long id, SegmentDTO dto) {
        Segment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Segmento não encontrado: " + id));

        if (!segment.getName().equals(dto.getName()) && segmentRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Segmento com este nome já existe: " + dto.getName());
        }

        segment.setName(dto.getName());
        segment.setDescription(dto.getDescription());

        return toDTO(segmentRepository.save(segment));
    }

    public void delete(Long id) {
        if (!segmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Segmento não encontrado: " + id);
        }
        segmentRepository.deleteById(id);
    }

    private SegmentDTO toDTO(Segment segment) {
        return new SegmentDTO(
                segment.getId(),
                segment.getName(),
                segment.getDescription()
        );
    }

    private Segment toEntity(SegmentDTO dto) {
        return new Segment(
                null,
                dto.getName(),
                dto.getDescription()
        );
    }
}
