package org.apollo.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.apollo.api.dto.MeDTO;
import org.apollo.api.dto.PhoneChangeRequestCreateDTO;
import org.apollo.api.dto.PhoneChangeRequestDTO;
import org.apollo.api.enums.PhoneChangeStatusEnum;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Employee;
import org.apollo.api.model.PhoneChangeRequest;
import org.apollo.api.repository.EmployeeRepository;
import org.apollo.api.repository.PhoneChangeRequestRepository;
import org.apollo.api.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhoneChangeServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MANAGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID UNIT_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
    private static final UUID REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-0000000000bb");

    @Mock
    private TenantContext tenantContext;

    @Mock
    private ProfileService profileService;

    @Mock
    private SettingsService settingsService;

    @Mock
    private PhoneChangeRequestRepository requestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private PhoneChangeService phoneChangeService;

    @Test
    void shouldRejectInvalidPhone() {
        when(profileService.me()).thenReturn(me());

        assertThrows(BusinessRuleException.class,
                () -> phoneChangeService.request(new PhoneChangeRequestCreateDTO("123")));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldRejectSamePhone() {
        when(profileService.me()).thenReturn(me());
        when(settingsService.currentPhone(USER_ID)).thenReturn("85987654321");

        assertThrows(BusinessRuleException.class,
                () -> phoneChangeService.request(new PhoneChangeRequestCreateDTO("(85) 98765-4321")));
    }

    @Test
    void shouldRejectSecondPendingRequest() {
        when(profileService.me()).thenReturn(me());
        when(settingsService.currentPhone(USER_ID)).thenReturn("85911112222");
        when(requestRepository.existsByEmployeeIdAndStatus(USER_ID, PhoneChangeStatusEnum.PENDING))
                .thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> phoneChangeService.request(new PhoneChangeRequestCreateDTO("(85) 98765-4321")));
    }

    @Test
    void shouldCreatePendingRequestWithDigitsOnly() {
        when(profileService.me()).thenReturn(me());
        when(settingsService.currentPhone(USER_ID)).thenReturn(null);
        when(requestRepository.existsByEmployeeIdAndStatus(USER_ID, PhoneChangeStatusEnum.PENDING))
                .thenReturn(false);
        when(requestRepository.save(any(PhoneChangeRequest.class))).thenAnswer(call -> call.getArgument(0));

        PhoneChangeRequestDTO result = phoneChangeService
                .request(new PhoneChangeRequestCreateDTO("(85) 98765-4321"));

        assertEquals("85987654321", result.newPhone());
        assertEquals(PhoneChangeStatusEnum.PENDING, result.status());
        assertEquals("Carlos Alves da Silva", result.employeeName());
    }

    @Test
    void shouldApproveAndUpdateThePhone() {
        PhoneChangeRequest request = pendingRequest();
        scopeOfManager();
        when(requestRepository.findByIdInScope(REQUEST_ID, 10L, UNIT_ID)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);
        when(tenantContext.getUserId()).thenReturn(MANAGER_ID);
        when(employeeRepository.findById(USER_ID)).thenReturn(Optional.of(employee()));

        PhoneChangeRequestDTO result = phoneChangeService.approve(REQUEST_ID);

        assertEquals(PhoneChangeStatusEnum.APPROVED, result.status());
        assertEquals(MANAGER_ID, request.getReviewedBy());
        verify(settingsService).setPhone(USER_ID, "85987654321");
    }

    @Test
    void shouldRejectWithoutChangingThePhone() {
        PhoneChangeRequest request = pendingRequest();
        scopeOfManager();
        when(requestRepository.findByIdInScope(REQUEST_ID, 10L, UNIT_ID)).thenReturn(Optional.of(request));
        when(requestRepository.save(request)).thenReturn(request);
        when(tenantContext.getUserId()).thenReturn(MANAGER_ID);
        when(employeeRepository.findById(USER_ID)).thenReturn(Optional.of(employee()));

        PhoneChangeRequestDTO result = phoneChangeService.reject(REQUEST_ID);

        assertEquals(PhoneChangeStatusEnum.REJECTED, result.status());
        verify(settingsService, never()).setPhone(any(), any());
    }

    @Test
    void shouldNotReviewTwice() {
        PhoneChangeRequest request = pendingRequest();
        request.setStatus(PhoneChangeStatusEnum.APPROVED);
        scopeOfManager();
        when(requestRepository.findByIdInScope(REQUEST_ID, 10L, UNIT_ID)).thenReturn(Optional.of(request));

        assertThrows(BusinessRuleException.class, () -> phoneChangeService.approve(REQUEST_ID));
    }

    @Test
    void shouldFailWhenRequestIsOutOfScope() {
        scopeOfManager();
        when(requestRepository.findByIdInScope(REQUEST_ID, 10L, UNIT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> phoneChangeService.reject(REQUEST_ID));
    }

    private void scopeOfManager() {
        when(tenantContext.getCompanyId()).thenReturn(10L);
        when(tenantContext.getCompanyUnitId()).thenReturn(UNIT_ID);
    }

    private PhoneChangeRequest pendingRequest() {
        PhoneChangeRequest request = new PhoneChangeRequest();
        request.setId(REQUEST_ID);
        request.setEmployeeId(USER_ID);
        request.setNewPhone("85987654321");
        return request;
    }

    private Employee employee() {
        Employee employee = new Employee();
        employee.setFullName("Carlos Alves da Silva");
        employee.setEmail("carlos@apollo.com");
        return employee;
    }

    private MeDTO me() {
        return new MeDTO(USER_ID, "Carlos Alves da Silva", "carlos@apollo.com", "TECHNICIAN", 10L,
                "Apollo Solar", UNIT_ID, "Fábrica Itajaí");
    }
}
