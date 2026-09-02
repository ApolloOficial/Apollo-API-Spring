package org.apollo.api.service;

import org.apollo.api.dto.AdministratorCreateDTO;
import org.apollo.api.dto.UserDTO;
import org.apollo.api.model.Company;
import org.apollo.api.model.Roles;
import org.apollo.api.model.User;
import org.apollo.api.repository.CompanyRepository;
import org.apollo.api.repository.RolesRepository;
import org.apollo.api.repository.UserRepository;
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
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RolesRepository rolesRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TenantContext tenantContext;
    @InjectMocks private UserService userService;

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
        when(userRepository.findByCompanyIdAndEmail(10L, request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByCompanyIdAndCpf(10L, request.getCpf())).thenReturn(Optional.empty());
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(rolesRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("bcrypt-hash");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO response = userService.create(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("bcrypt-hash", savedUser.getPassword());
        assertEquals(10L, savedUser.getCompany().getId());
        assertEquals(10L, response.getCompanyId());
    }
}
