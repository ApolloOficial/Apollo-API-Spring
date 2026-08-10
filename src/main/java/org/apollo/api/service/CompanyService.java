package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.CompanyDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Company;
import org.apollo.api.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<CompanyDTO> findAll() {
        return companyRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public CompanyDTO findById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));
        return toDTO(company);
    }

    public CompanyDTO findByName(String name) {
        Company company = companyRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + name));
        return toDTO(company);
    }

    public CompanyDTO create(CompanyDTO dto) {
        if (companyRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Empresa com este nome já existe: " + dto.getName());
        }
        Company company = toEntity(dto);
        return toDTO(companyRepository.save(company));
    }

    public CompanyDTO update(Long id, CompanyDTO dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));

        if (!company.getName().equals(dto.getName()) && companyRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Empresa com este nome já existe: " + dto.getName());
        }

        company.setName(dto.getName());
        company.setDescription(dto.getDescription());

        return toDTO(companyRepository.save(company));
    }

    public void delete(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa não encontrada: " + id);
        }
        companyRepository.deleteById(id);
    }

    private CompanyDTO toDTO(Company company) {
        return new CompanyDTO(
                company.getId(),
                company.getName(),
                company.getDescription()
        );
    }

    private Company toEntity(CompanyDTO dto) {
        return new Company(
                null,
                dto.getName(),
                dto.getDescription()
        );
    }
}
