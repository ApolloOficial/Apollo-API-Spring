package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.AddressDTO;
import org.apollo.api.dto.CompanyUnitDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Address;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.model.Segment;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.SegmentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyUnitService {

    private final CompanyUnitRepository companyUnitRepository;
    private final SegmentRepository segmentRepository;

    public List<CompanyUnitDTO> findAll() {
        return companyUnitRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public CompanyUnitDTO findById(Long id) {
        CompanyUnit unit = companyUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada: " + id));
        return toDTO(unit);
    }

    public List<CompanyUnitDTO> findBySegmentId(Long segmentId) {
        if (!segmentRepository.existsById(segmentId)) {
            throw new ResourceNotFoundException("Segmento não encontrado: " + segmentId);
        }
        return companyUnitRepository.findBySegmentId(segmentId).stream()
                .map(this::toDTO)
                .toList();
    }

    public CompanyUnitDTO create(CompanyUnitDTO dto) {
        Segment segment = segmentRepository.findById(dto.getSegmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Segmento não encontrado: " + dto.getSegmentId()));

        CompanyUnit unit = new CompanyUnit();
        unit.setSegment(segment);
        unit.setAddress(toAddressEntity(dto.getAddress()));
        unit.setName(dto.getName());
        unit.setCreatedAt(LocalDate.now());
        unit.setEmail(dto.getEmail());
        unit.setPhone(dto.getPhone());

        // cascade = CascadeType.ALL no relacionamento com Address salva o endereço junto
        return toDTO(companyUnitRepository.save(unit));
    }

    public CompanyUnitDTO update(Long id, CompanyUnitDTO dto) {
        CompanyUnit unit = companyUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada: " + id));

        Segment segment = segmentRepository.findById(dto.getSegmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Segmento não encontrado: " + dto.getSegmentId()));

        Address address = unit.getAddress();
        address.setStreetName(dto.getAddress().getStreetName());
        address.setNumber(dto.getAddress().getNumber());
        address.setAdditionalInfo(dto.getAddress().getAdditionalInfo());
        address.setNeighborhood(dto.getAddress().getNeighborhood());
        address.setCity(dto.getAddress().getCity());
        address.setState(dto.getAddress().getState());
        address.setZipCode(dto.getAddress().getZipCode());

        unit.setSegment(segment);
        unit.setName(dto.getName());
        unit.setEmail(dto.getEmail());
        unit.setPhone(dto.getPhone());

        // cascade = CascadeType.ALL propaga o update do endereço junto com a unidade
        return toDTO(companyUnitRepository.save(unit));
    }

    public void delete(Long id) {
        if (!companyUnitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Unidade não encontrada: " + id);
        }
        companyUnitRepository.deleteById(id);
    }

    private CompanyUnitDTO toDTO(CompanyUnit unit) {
        return new CompanyUnitDTO(
                unit.getId(),
                unit.getSegment().getId(),
                unit.getSegment().getName(),
                toAddressDTO(unit.getAddress()),
                unit.getName(),
                unit.getCreatedAt(),
                unit.getEmail(),
                unit.getPhone()
        );
    }

    private Address toAddressEntity(AddressDTO dto) {
        Address address = new Address();
        address.setStreetName(dto.getStreetName());
        address.setNumber(dto.getNumber());
        address.setAdditionalInfo(dto.getAdditionalInfo());
        address.setNeighborhood(dto.getNeighborhood());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        return address;
    }

    private AddressDTO toAddressDTO(Address address) {
        return new AddressDTO(
                address.getId(),
                address.getStreetName(),
                address.getNumber(),
                address.getAdditionalInfo(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getZipCode()
        );
    }
}
