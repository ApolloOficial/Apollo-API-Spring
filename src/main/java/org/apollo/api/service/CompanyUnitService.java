package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.AddressDTO;
import org.apollo.api.dto.CompanyUnitDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Address;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.model.Employee;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.security.TenantContext;
import org.apollo.api.util.Specs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyUnitService {

    private final CompanyUnitRepository companyUnitRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public Page<CompanyUnitDTO> findAll(String name, String city, String state, Boolean active,
                                        Long segmentId, Pageable pageable) {
        Specification<CompanyUnit> spec = Specification
                .where(Specs.<CompanyUnit>equalTo(u -> u.get("company").get("id"), companyId()))
                .and(Specs.<CompanyUnit>contains(u -> u.get("name"), name))
                .and(Specs.<CompanyUnit>contains(u -> u.get("address").get("city"), city))
                .and(Specs.<CompanyUnit>equalTo(u -> u.get("address").get("state"),
                        state == null || state.isBlank() ? null : state.trim().toUpperCase()))
                .and(Specs.<CompanyUnit>equalTo(u -> u.get("active"), active))
                .and(Specs.<CompanyUnit>equalTo(u -> u.get("segment").get("id"), segmentId));
        return companyUnitRepository.findAll(spec, pageable).map(this::toDTO);
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

    public CompanyUnitDTO update(UUID id, CompanyUnitDTO dto) {
        CompanyUnit unit = findUnit(id);
        // Segmento e CNPJ vem do cadastro do 1o ano e nao mudam por aqui.
        updateAddress(unit.getAddress(), dto.getAddress());
        applyFields(unit, dto);
        return toDTO(companyUnitRepository.saveAndFlush(unit));
    }

    private CompanyUnit findUnit(UUID id) {
        return companyUnitRepository.findByIdAndCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + id));
    }

    private Employee findEmployee(UUID employeeId) {
        return employeeRepository.findByIdAndCompanyUnitCompanyId(employeeId, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private void applyFields(CompanyUnit unit, CompanyUnitDTO dto) {
        unit.setName(dto.getName());
        unit.setEmail(dto.getEmail());
        unit.setPhone(dto.getPhone());
        unit.setContactEmail(dto.getContactEmail());
        unit.setContactPhone(dto.getContactPhone());
        unit.setActive(dto.getActive() == null || dto.getActive());
        unit.setResponsibleEmployee(dto.getResponsibleEmployeeId() == null
                ? null
                : findEmployee(dto.getResponsibleEmployeeId()));
    }

    private CompanyUnitDTO toDTO(CompanyUnit unit) {
        CompanyUnitDTO dto = new CompanyUnitDTO(
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
        dto.setResponsibleEmployeeName(unit.getResponsibleEmployee() != null
                ? unit.getResponsibleEmployee().getFullName() : null);
        return dto;
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
