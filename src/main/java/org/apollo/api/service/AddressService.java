package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.AddressDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Address;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.repository.AddressRepository;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * SECURITY BUG FIX (cross-tenant data leak): "address" has no company_id column of its
 * own - it's only tenant-owned indirectly, through the company_unit that references it
 * (company_unit.address_id). This service used to read/update/delete any address by ID
 * with no ownership check at all, so any authenticated manager could view or modify the
 * address of a company_unit belonging to a completely different tenant.
 * <p>
 * An address can also legitimately be "unclaimed" for a short window: company_unit.
 * address_id is NOT NULL, so the normal flow is POST /addresses first, then POST
 * /company-units referencing that address id. To keep that flow working, access is
 * allowed when an address is not yet linked to any company_unit; once it IS linked, only
 * the owning tenant may read/modify/delete it.
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final TenantContext tenantContext;

    public Page<AddressDTO> findAll(Pageable pageable) {
        return addressRepository.findAllForCompany(companyId(), pageable).map(this::toDTO);
    }

    public AddressDTO findById(Long id) {
        return toDTO(requireAccessible(findAddress(id)));
    }

    public AddressDTO create(AddressDTO dto) {
        Address address = toEntity(dto);
        return toDTO(addressRepository.save(address));
    }

    public AddressDTO update(Long id, AddressDTO dto) {
        Address address = requireAccessible(findAddress(id));

        address.setStreetName(dto.getStreetName());
        address.setNumber(dto.getNumber());
        address.setAdditionalInfo(dto.getAdditionalInfo());
        address.setNeighborhood(dto.getNeighborhood());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());

        return toDTO(addressRepository.save(address));
    }

    public void delete(Long id) {
        Address address = requireAccessible(findAddress(id));
        addressRepository.delete(address);
    }

    private Address findAddress(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
    }

    private Address requireAccessible(Address address) {
        Optional<CompanyUnit> owningUnit = companyUnitRepository.findByAddressId(address.getId());
        if (owningUnit.isPresent() && !owningUnit.get().getCompany().getId().equals(companyId())) {
            // Reported as 404: an address belonging to another tenant should not even
            // be confirmed to exist.
            throw new ResourceNotFoundException("Address not found: " + address.getId());
        }
        return address;
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private AddressDTO toDTO(Address address) {
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

    private Address toEntity(AddressDTO dto) {
        return new Address(
                null,
                dto.getStreetName(),
                dto.getNumber(),
                dto.getAdditionalInfo(),
                dto.getNeighborhood(),
                dto.getCity(),
                dto.getState(),
                dto.getZipCode()
        );
    }
}
