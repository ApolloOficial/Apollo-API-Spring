package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatbotSourceDTO(String documento, Integer pagina, String secao, String url, String trecho,
                               Double score) {
}
