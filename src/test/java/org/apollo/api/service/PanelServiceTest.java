package org.apollo.api.service;

import org.apollo.api.dto.PanelDTO;
import org.apollo.api.enums.OperatingStatsEnum;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.Batch;
import org.apollo.api.model.Panel;
import org.apollo.api.repository.BatchRepository;
import org.apollo.api.repository.PanelRepository;
import org.apollo.api.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PanelServiceTest {
    @Mock private PanelRepository panelRepository;
    @Mock private BatchRepository batchRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private PanelService panelService;

    @Test
    void shouldListPanelsOnlyFromAuthenticatedTenant() {
        when(tenantContext.getCompanyId()).thenReturn(10L);
        when(panelRepository.findAllByBatchCompanyUnitCompanyId(10L)).thenReturn(List.of());
        panelService.findAll();
        verify(panelRepository).findAllByBatchCompanyUnitCompanyId(10L);
    }

    @Test
    void shouldRejectPanelFromAnotherTenant() {
        when(tenantContext.getCompanyId()).thenReturn(10L);
        when(panelRepository.findByIdAndBatchCompanyUnitCompanyId(99L, 10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> panelService.findById(99L));
    }

    @Test
    void shouldCreateStockPanelWithoutInstallationDate() {
        UUID batchId = UUID.randomUUID();
        PanelDTO request = new PanelDTO(1L, batchId, 25, "APL-0000001", "7890000000001", OperatingStatsEnum.EM_ESTOQUE, new BigDecimal("20.00"), null);
        Batch batch = new Batch();
        batch.setId(batchId);
        when(tenantContext.getCompanyId()).thenReturn(10L);
        when(batchRepository.findByIdAndCompanyUnitCompanyId(batchId, 10L)).thenReturn(Optional.of(batch));
        when(panelRepository.save(any(Panel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PanelDTO response = panelService.create(request);

        ArgumentCaptor<Panel> panelCaptor = ArgumentCaptor.forClass(Panel.class);
        verify(panelRepository).save(panelCaptor.capture());
        assertNull(panelCaptor.getValue().getInstallationDt());
        assertEquals(OperatingStatsEnum.EM_ESTOQUE, response.getOperatingStats());
    }
}