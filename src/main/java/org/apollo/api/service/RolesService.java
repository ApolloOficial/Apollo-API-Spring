package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.RolesDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Roles;
import org.apollo.api.repository.RolesRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolesService {

    private final RolesRepository rolesRepository;

    public List<RolesDTO> findAll() {
        return rolesRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public RolesDTO findById(Long id) {
        Roles roles = rolesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado: " + id));
        return toDTO(roles);
    }

    public RolesDTO create(RolesDTO dto) {
        if (rolesRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Perfil com este nome já existe: " + dto.getName());
        }
        Roles roles = toEntity(dto);
        return toDTO(rolesRepository.save(roles));
    }

    public RolesDTO update(Long id, RolesDTO dto) {
        Roles roles = rolesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado: " + id));

        if (!roles.getName().equals(dto.getName()) && rolesRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Perfil com este nome já existe: " + dto.getName());
        }

        roles.setName(dto.getName());
        roles.setDescription(dto.getDescription());

        return toDTO(rolesRepository.save(roles));
    }

    public void delete(Long id) {
        if (!rolesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Perfil não encontrado: " + id);
        }
        rolesRepository.deleteById(id);
    }

    private RolesDTO toDTO(Roles roles) {
        return new RolesDTO(
                roles.getId(),
                roles.getName(),
                roles.getDescription()
        );
    }

    private Roles toEntity(RolesDTO dto) {
        return new Roles(
                null,
                dto.getName(),
                dto.getDescription()
        );
    }
}
