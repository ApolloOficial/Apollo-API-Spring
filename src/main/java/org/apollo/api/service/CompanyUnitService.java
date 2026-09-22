package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.AddressDTO;
import org.apollo.api.dto.CompanyUnitDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Address;
import org.apollo.api.model.Company;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.model.Employee;
import org.apollo.api.model.Segment;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.repository.SegmentRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyUnitService {

    private final CompanyUnitRepository companyUnitRepository;
    private final SegmentRepository segmentRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public List<CompanyUnitDTO> findAll() {
        return companyUnitRepository.findAllByCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public CompanyUnitDTO findById(UUID id) {
        return toDTO(findUnit(id));
    }

    @Transactional(readOnly = true)
    public List<CompanyUnitDTO> findBySegmentId(Long segmentId) {
        return companyUnitRepository.findBySegmentIdAndCompanyId(segmentId, companyId()).stream()
                .map(this::toDTO)
                .toList();
    }

    public CompanyUnitDTO create(CompanyUnitDTO dto) {
        Company company = companyRepository.findById(companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + companyId()));
        CompanyUnit unit = new CompanyUnit();
        unit.setCompany(company);
        unit.setSegment(findSegment(dto.getSegmentId()));
        unit.setAddress(toAddressEntity(dto.getAddress()));
        unit.setCreatedAt(LocalDate.now());
        applyFields(unit, dto);
        return toDTO(companyUnitRepository.save(unit));
    }

    public CompanyUnitDTO update(UUID id, CompanyUnitDTO dto) {
        CompanyUnit unit = findUnit(id);
        unit.setSegment(findSegment(dto.getSegmentId()));
        updateAddress(unit.getAddress(), dto.getAddress());
        applyFields(unit, dto);
        return toDTO(companyUnitRepository.save(unit));
    }

    public void delete(UUID id) {
        companyUnitRepository.delete(findUnit(id));
    }

    private CompanyUnit findUnit(UUID id) {
        return companyUnitRepository.findByIdAndCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada: " + id));
    }

    private Segment findSegment(Long segmentId) {
        return segmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Segmento não encontrado: " + segmentId));
    }

    private Employee findEmployee(UUID employeeId) {
        return employeeRepository.findByIdAndCompanyUnitCompanyId(employeeId, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + employeeId));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private void applyFields(CompanyUnit unit, CompanyUnitDTO dto) {
        unit.setName(dto.getName());
        unit.setEmail(dto.getEmail());
        unit.setPhone(dto.getPhone());
        unit.setCnpj(dto.getCnpj());
        unit.setContactEmail(dto.getContactEmail());
        unit.setContactPhone(dto.getContactPhone());
        unit.setKwpTotal(dto.getKwpTotal());
        unit.setActive(dto.getActive() == null || dto.getActive());
        unit.setResponsibleEmployee(dto.getResponsibleEmployeeId() == null
                ? null
                : findEmployee(dto.getResponsibleEmployeeId()));
    }

    private CompanyUnitDTO toDTO(CompanyUnit unit) {
        return new CompanyUnitDTO(
                unit.getId(),
                unit.getCompany().getId(),
                unit.getSegment().getId(),
                unit.getSegment().getName(),
                toAddressDTO(unit.getAddress()),
                unit.getName(),
                unit.getCreatedAt(),
                unit.getEmail(),
                unit.getPhone(),
                unit.getCnpj(),
                unit.getResponsibleEmployee() != null ? unit.getResponsibleEmployee().getId() : null,
                unit.getContactEmail(),
                unit.getContactPhone(),
                unit.getKwpTotal(),
                unit.isActive()
        );
    }

    private Address toAddressEntity(AddressDTO dto) {
        Address address = new Address();
        updateAddress(address, dto);
        return address;
    }

    private void updateAddress(Address address, AddressDTO dto) {
        address.setStreetName(dto.getStreetName());
        address.setNumber(dto.getNumber());
        address.setAdditionalInfo(dto.getAdditionalInfo());
        address.setNeighborhood(dto.getNeighborhood());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
    }

    private AddressDTO toAddressDTO(Address address) {
        return new AddressDTO(address.getId(), address.getStreetName(), address.getNumber(),
                address.getAdditionalInfo(), address.getNeighborhood(), address.getCity(),
                address.getState(), address.getZipCode());
    }
}
