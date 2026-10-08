package org.apollo.api.dto;

public record ChatSourceDTO(String document, Integer page, String section, String url, String excerpt,
                            Double score) {
}
