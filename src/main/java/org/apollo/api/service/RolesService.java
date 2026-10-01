package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.RolesDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Roles;
import org.apollo.api.repository.RolesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cargos (GERENTE, ANALISTA, OPERADOR, TECNICO): catalogo fixo do banco, so leitura.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolesService {

    private final RolesRepository rolesRepository;

    public Page<RolesDTO> findAll(Pageable pageable) {
        return rolesRepository.findAll(pageable).map(this::toDTO);
    }

    public RolesDTO findById(Long id) {
        return toDTO(rolesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id)));
    }

    private RolesDTO toDTO(Roles roles) {
        return new RolesDTO(roles.getId(), roles.getName(), roles.getDescription());
    }
}
