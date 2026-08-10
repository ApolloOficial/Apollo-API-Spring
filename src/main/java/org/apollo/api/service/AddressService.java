package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.AddressDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Address;
import org.apollo.api.repository.AddressRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public List<AddressDTO> findAll() {
        return addressRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public AddressDTO findById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));
        return toDTO(address);
    }

    public AddressDTO create(AddressDTO dto) {
        Address address = toEntity(dto);
        return toDTO(addressRepository.save(address));
    }

    public AddressDTO update(Long id, AddressDTO dto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));

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
        if (!addressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Endereço não encontrado: " + id);
        }
        addressRepository.deleteById(id);
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
