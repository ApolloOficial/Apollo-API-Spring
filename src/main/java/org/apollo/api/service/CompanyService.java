package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.CompanyDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Company;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Empresa (tenant). Vem do cadastro do 1o ano, entao aqui so leitura; e cada usuario so
 * enxerga a PROPRIA empresa (id de outra empresa responde 404, para nao confirmar que existe).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final TenantContext tenantContext;

    // Sempre no maximo 1 item (a empresa do usuario); Page so para manter o padrao da API.
    public Page<CompanyDTO> findAll(Pageable pageable) {
        return new PageImpl<>(List.of(toDTO(ownCompany())), pageable, 1);
    }

    public CompanyDTO findById(Long id) {
        if (!id.equals(companyId())) {
            throw new ResourceNotFoundException("Company not found: " + id);
        }
        return toDTO(ownCompany());
    }

    public CompanyDTO findByName(String name) {
        Company company = companyRepository.findByName(name)
                .filter(c -> c.getId().equals(companyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + name));
        return toDTO(company);
    }

    private Company ownCompany() {
        return companyRepository.findById(companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId()));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private CompanyDTO toDTO(Company c) {
        return new CompanyDTO(c.getId(), c.getName(), c.getCnpj(), c.getContactEmail(), c.getDescription(), c.isActive());
    }
}
