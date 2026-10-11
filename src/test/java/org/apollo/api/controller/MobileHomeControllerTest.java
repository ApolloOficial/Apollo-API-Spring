package org.apollo.api.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.apollo.api.dto.MobileHomeAttentionItemDTO;
import org.apollo.api.dto.MobileHomeDTO;
import org.apollo.api.dto.MobileHomePendingRegistrationDTO;
import org.apollo.api.dto.MobileHomeServiceOrderDTO;
import org.apollo.api.service.MobileHomeService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MobileHomeControllerTest {

    @Test
    void shouldExposeTheFieldsTheMobileAppReads() throws Exception {
        MobileHomeService service = mock(MobileHomeService.class);
        when(service.home()).thenReturn(new MobileHomeDTO("Ana Souza", null, "Filial Centro", "Campinas", "SP",
                1, List.of(new MobileHomeServiceOrderDTO(7, "OS-0007", "SN-1", "PENDING", "HIGH", "2026-10-20")),
                1, List.of(new MobileHomeAttentionItemDTO(11, "HIGH", "Low efficiency", "Saúde da string", 62.5, 7)),
                List.of(new MobileHomePendingRegistrationDTO(9, "S-01", 4, 10))));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MobileHomeController(service)).build();

        mockMvc.perform(get("/api/v1/mobile/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianName").value("Ana Souza"))
                .andExpect(jsonPath("$.siteName").value("Filial Centro"))
                .andExpect(jsonPath("$.siteCity").value("Campinas"))
                .andExpect(jsonPath("$.siteState").value("SP"))
                .andExpect(jsonPath("$.totalServiceOrders").value(1))
                .andExpect(jsonPath("$.serviceOrders[0].code").value("OS-0007"))
                .andExpect(jsonPath("$.serviceOrders[0].assetCode").value("SN-1"))
                .andExpect(jsonPath("$.serviceOrders[0].status").value("PENDING"))
                .andExpect(jsonPath("$.serviceOrders[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.serviceOrders[0].dueDate").value("2026-10-20"))
                .andExpect(jsonPath("$.totalAttentionItems").value(1))
                .andExpect(jsonPath("$.attentionItems[0].metricValue").value(62.5))
                .andExpect(jsonPath("$.attentionItems[0].serviceOrderId").value(7))
                .andExpect(jsonPath("$.pendingRegistrations[0].batchCode").value("S-01"))
                .andExpect(jsonPath("$.pendingRegistrations[0].registered").value(4))
                .andExpect(jsonPath("$.pendingRegistrations[0].total").value(10));
    }
}
