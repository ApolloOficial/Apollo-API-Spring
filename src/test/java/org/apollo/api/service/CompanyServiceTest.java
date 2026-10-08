package org.apollo.api.service;

import org.apollo.api.dto.CompanyDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Company;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Regression tests for the cross-tenant leak: every company row IS a tenant, so a
// manager must only ever see / modify / delete their own company.
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {
    @Mock private CompanyRepository companyRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private CompanyService companyService;

    @Test
    void shouldListOnlyTheAuthenticatedTenantsCompany() {
        when(tenantContext.getCompanyId()).thenReturn(1L);
        Company own = new Company();
        own.setId(1L);
        own.setName("Own Company");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(own));

        Page<CompanyDTO> result = companyService.findAll(PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).id());
    }

    @Test
    void shouldHideAnotherTenantsCompanyOnFindById() {
        when(tenantContext.getCompanyId()).thenReturn(1L);
        assertThrows(ResourceNotFoundException.class, () -> companyService.findById(2L));
        verify(companyRepository, never()).findById(2L);
    }
}
