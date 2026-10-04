package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatbotContextDTO(String componente, String fabricante, String modelo, String serial,
                                String barcode, String sintoma, String alerta, String medicoes,
                                @JsonProperty("condicao_ambiental") String condicaoAmbiental,
                                @JsonProperty("procedimento_executado") String procedimentoExecutado) {
}
