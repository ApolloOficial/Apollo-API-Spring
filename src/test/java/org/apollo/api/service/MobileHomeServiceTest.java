package org.apollo.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apollo.api.dto.MobileHomeDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.repository.MobileHomeRepository;
import org.apollo.api.repository.MobileHomeRepository.AttentionRow;
import org.apollo.api.repository.MobileHomeRepository.HeaderRow;
import org.apollo.api.repository.MobileHomeRepository.OrderRow;
import org.apollo.api.repository.MobileHomeRepository.Page;
import org.apollo.api.repository.MobileHomeRepository.PendingRow;
import org.apollo.api.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MobileHomeServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID UNIT_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    @Mock
    private MobileHomeRepository repository;

    @Mock
    private TenantContext tenantContext;

    @InjectMocks
    private MobileHomeService service;

    private void givenUser(String role, UUID unitId) {
        when(tenantContext.getUserId()).thenReturn(USER_ID);
        when(tenantContext.getCompanyUnitId()).thenReturn(unitId);
        when(tenantContext.getRoleName()).thenReturn(role);
    }

    private void givenHeader(boolean hasPhoto) {
        when(repository.findHeader(USER_ID))
                .thenReturn(Optional.of(new HeaderRow("Ana Souza", "Filial Centro", "Campinas", "SP", hasPhoto)));
    }

    @Test
    void shouldBuildHomeForTechnicianScopedToOwnOrders() {
        givenUser("TECHNICIAN", UNIT_ID);
        givenHeader(true);
        when(repository.findOpenOrders(UNIT_ID, USER_ID, MobileHomeService.ORDERS_LIMIT)).thenReturn(new Page<>(
                List.of(new OrderRow(7, "EM_ANDAMENTO", LocalDate.of(2026, 10, 20), "CRITICA", "SN-1")), 3));
        when(repository.findAttention(UNIT_ID, USER_ID, MobileHomeService.ATTENTION_LIMIT)).thenReturn(new Page<>(
                List.of(new AttentionRow(11, "ALTA", "Low efficiency", 62.5, 7L)), 1));
        when(repository.findPendingRegistrations(UNIT_ID, MobileHomeService.PENDING_LIMIT)).thenReturn(new Page<>(
                List.of(new PendingRow(UUID.fromString("00000000-0000-0000-0000-000000000009"), "S-01", 4, 10)), 1));

        MobileHomeDTO home = service.home();

        assertEquals("Ana Souza", home.technicianName());
        assertEquals(MobileHomeService.PHOTO_PATH, home.profileImageUrl());
        assertEquals("Filial Centro", home.siteName());
        assertEquals("Campinas", home.siteCity());
        assertEquals("SP", home.siteState());
        assertEquals(3, home.totalServiceOrders());
        assertEquals(1, home.serviceOrders().size());
        assertEquals("OS-0007", home.serviceOrders().getFirst().code());
        assertEquals("SN-1", home.serviceOrders().getFirst().assetCode());
        assertEquals("IN_PROGRESS", home.serviceOrders().getFirst().status());
        assertEquals("CRITICAL", home.serviceOrders().getFirst().priority());
        assertEquals("2026-10-20", home.serviceOrders().getFirst().dueDate());
        assertEquals(1, home.totalAttentionItems());
        assertEquals("HIGH", home.attentionItems().getFirst().severity());
        assertEquals(MobileHomeService.HEALTH_LABEL, home.attentionItems().getFirst().metricLabel());
        assertEquals(62.5, home.attentionItems().getFirst().metricValue());
        assertEquals(7L, home.attentionItems().getFirst().serviceOrderId());
        assertEquals("S-01", home.pendingRegistrations().getFirst().batchCode());
        assertEquals(4, home.pendingRegistrations().getFirst().registered());
        assertEquals(10, home.pendingRegistrations().getFirst().total());
        assertEquals(0L, home.pendingRegistrations().getFirst().batchId() >> 63);
    }

    @Test
    void shouldNotScopeByTechnicianForOtherRoles() {
        givenUser("MANAGER", UNIT_ID);
        givenHeader(false);
        when(repository.findOpenOrders(UNIT_ID, null, MobileHomeService.ORDERS_LIMIT))
                .thenReturn(new Page<>(List.of(), 0));
        when(repository.findAttention(UNIT_ID, null, MobileHomeService.ATTENTION_LIMIT))
                .thenReturn(new Page<>(List.of(), 0));
        when(repository.findPendingRegistrations(UNIT_ID, MobileHomeService.PENDING_LIMIT))
                .thenReturn(new Page<>(List.of(), 0));

        MobileHomeDTO home = service.home();

        assertNull(home.profileImageUrl());
        assertEquals(0, home.totalServiceOrders());
        assertEquals(0, home.totalAttentionItems());
        assertEquals(List.of(), home.serviceOrders());
    }

    @Test
    void shouldReturnEmptyListsWhenUserHasNoUnit() {
        givenUser("MANAGER", null);
        givenHeader(false);

        MobileHomeDTO home = service.home();

        assertEquals(0, home.totalServiceOrders());
        assertEquals(List.of(), home.attentionItems());
        assertEquals(List.of(), home.pendingRegistrations());
        verify(repository, never()).findOpenOrders(any(), any(), eq(MobileHomeService.ORDERS_LIMIT));
    }

    @Test
    void shouldReportMissingMeasurementAsZeroProgress() {
        givenUser("OPERATOR", UNIT_ID);
        givenHeader(false);
        when(repository.findOpenOrders(UNIT_ID, null, MobileHomeService.ORDERS_LIMIT))
                .thenReturn(new Page<>(List.of(), 0));
        when(repository.findAttention(UNIT_ID, null, MobileHomeService.ATTENTION_LIMIT)).thenReturn(new Page<>(
                List.of(new AttentionRow(5, "BAIXA", "Panel issue", null, null)), 1));
        when(repository.findPendingRegistrations(UNIT_ID, MobileHomeService.PENDING_LIMIT))
                .thenReturn(new Page<>(List.of(), 0));

        MobileHomeDTO home = service.home();

        assertEquals(MobileHomeService.NO_MEASUREMENT_LABEL, home.attentionItems().getFirst().metricLabel());
        assertEquals(0d, home.attentionItems().getFirst().metricValue());
        assertEquals(0L, home.attentionItems().getFirst().serviceOrderId());
        assertEquals("LOW", home.attentionItems().getFirst().severity());
    }

    @Test
    void shouldFailWhenUserDoesNotExist() {
        when(tenantContext.getUserId()).thenReturn(USER_ID);
        when(tenantContext.getCompanyUnitId()).thenReturn(UNIT_ID);
        when(repository.findHeader(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.home());
    }

    @Test
    void shouldMapStatusAndPriorityToMobileContract() {
        assertEquals("PENDING", MobileHomeService.status("ABERTA"));
        assertEquals("IN_PROGRESS", MobileHomeService.status("EM_ANDAMENTO"));
        assertEquals("COMPLETED", MobileHomeService.status("CONCLUIDA"));
        assertEquals("CANCELLED", MobileHomeService.status("CANCELADA"));
        assertEquals("LOW", MobileHomeService.priority("BAIXA"));
        assertEquals("MEDIUM", MobileHomeService.priority("MEDIA"));
        assertEquals("HIGH", MobileHomeService.priority("ALTA"));
        assertEquals("CRITICAL", MobileHomeService.priority("CRITICA"));
        assertEquals("MEDIUM", MobileHomeService.priority(null));
    }
}
