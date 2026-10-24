package org.apollo.api.sungrow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** Resposta de "Query Device Real-Time Measuring Point Data". Cada ponto vem como p{id}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SungrowRealTimeResponse(@JsonProperty("result_code") String resultCode,
                                      @JsonProperty("result_msg") String resultMsg,
                                      String error,
                                      @JsonProperty("error_description") String errorDescription,
                                      @JsonProperty("result_data") RealTimeData resultData) implements SungrowEnvelope {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RealTimeData(@JsonProperty("device_point_list") List<Map<String, Object>> devicePointList) {
    }
}
