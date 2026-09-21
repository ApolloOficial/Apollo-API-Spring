package org.apollo.api.service;

import org.apollo.api.dto.AdministratorCreateDTO;
import org.apollo.api.dto.AdministratorDTO;
import org.apollo.api.model.Administrator;
import org.apollo.api.model.Company;
import org.apollo.api.model.Roles;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.repository.RolesRepository;
import org.apollo.api.repository.AdministratorRepository;
import org.apollo.api.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministratorServiceTest {

    @Mock private AdministratorRepository administratorRepository;
    @Mock private RolesRepository rolesRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TenantContext tenantContext;
    @InjectMocks private AdministratorService administratorService;

    @Test
    void shouldEncodePasswordAndUseAuthenticatedTenantWhenCreatingAdministrator() {
        AdministratorCreateDTO request = new AdministratorCreateDTO();
        request.setRoleId(1L);
        request.setFullName("Administrador Apollo");
        request.setEmail("admin@apollo.local");
        request.setCpf("12345678901");
        request.setPassword("SenhaSegura123");
        Company company = new Company(10L, "Apollo", null);
        Roles role = new Roles(1L, "ADMINISTRATOR", null);

        when(tenantContext.getCompanyId()).thenReturn(10L);
        when(administratorRepository.findByCompanyIdAndEmail(10L, request.getEmail())).thenReturn(Optional.empty());
        when(administratorRepository.findByCompanyIdAndCpf(10L, request.getCpf())).thenReturn(Optional.empty());
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(rolesRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("bcrypt-hash");
        when(administratorRepository.save(org.mockito.ArgumentMatchers.any(Administrator.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdministratorDTO response = administratorService.create(request);

        ArgumentCaptor<Administrator> userCaptor = ArgumentCaptor.forClass(Administrator.class);
        verify(administratorRepository).save(userCaptor.capture());
        Administrator savedAdministrator = userCaptor.getValue();
        assertEquals("bcrypt-hash", savedAdministrator.getPassword());
        assertEquals(10L, savedAdministrator.getCompany().getId());
        assertEquals(10L, response.getCompanyId());
    }
}
