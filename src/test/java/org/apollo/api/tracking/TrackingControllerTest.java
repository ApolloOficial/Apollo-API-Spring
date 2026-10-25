package org.apollo.api.tracking;

import org.apollo.api.exception.GlobalExceptionHandler;
import org.apollo.api.tracking.config.TrackingBodyLimitFilter;
import org.apollo.api.tracking.controller.TrackingController;
import org.apollo.api.tracking.dto.TrackingBatchResponse;
import org.apollo.api.tracking.service.TrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrackingControllerTest {
    @Test
    void parsesBatchAndRejectsEmptyOrOversizedBody() throws Exception {
        TrackingService service = mock(TrackingService.class);
        UUID id = UUID.randomUUID();
        when(service.accept(any())).thenReturn(new TrackingBatchResponse(List.of(id)));
        try (var validator = new LocalValidatorFactoryBean()) {
            validator.afterPropertiesSet();
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new TrackingController(service))
                    .setValidator(validator).setControllerAdvice(new GlobalExceptionHandler())
                    .addFilters(new TrackingBodyLimitFilter()).build();
            String json = """
                    {"events":[{"event_id":"%s","event_name":"screen_viewed",
                    "session_id":"%s","screen":"HomeActivity",
                    "timestamp":"2026-10-23T10:30:00Z","platform":"android","properties":{}}]}
                    """.formatted(id, UUID.randomUUID());
            mvc.perform(post("/api/tracking/events/batch").contentType("application/json").content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accepted_event_ids[0]").value(id.toString()));
            mvc.perform(post("/api/tracking/events/batch").contentType("application/json")
                            .content("{\"events\":[]}"))
                    .andExpect(status().isBadRequest());
            mvc.perform(post("/api/tracking/events/batch").servletPath("/api/tracking/events/batch")
                            .contentType("application/json").content(" ".repeat(128 * 1024 + 1)))
                    .andExpect(status().isPayloadTooLarge());
            verify(service, times(1)).accept(any());
        }
    }
}
