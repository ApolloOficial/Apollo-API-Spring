package org.apollo.api.controller;

import org.apollo.api.exception.GlobalExceptionHandler;
import org.apollo.api.service.InverterService;
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

    private InverterService inverterService;
    private PanelService panelService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        inverterService = mock(InverterService.class);
        panelService = mock(PanelService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new InverterController(inverterService),
                        new PanelController(panelService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRejectInvalidInverterBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/inverters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(inverterService);
    }

    @Test
    void shouldRejectInvalidPanelActivationBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/panels/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(panelService);
    }
}
