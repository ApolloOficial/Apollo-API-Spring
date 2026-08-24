package org.apollo.api.controller;

import org.apollo.api.exception.GlobalExceptionHandler;
import org.apollo.api.service.BatchService;
import org.apollo.api.service.PanelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ValidationControllerTest {

    private BatchService batchService;
    private PanelService panelService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        batchService = mock(BatchService.class);
        panelService = mock(PanelService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new BatchController(batchService),
                        new PanelController(panelService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRejectInvalidBatchBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(batchService);
    }

    @Test
    void shouldRejectInvalidPanelBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(panelService);
    }
}
