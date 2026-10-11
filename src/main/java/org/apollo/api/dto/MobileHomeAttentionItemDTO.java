package org.apollo.api.dto;

public record MobileHomeAttentionItemDTO(long id, String severity, String title, String metricLabel,
                                         double metricValue, long serviceOrderId) {
}
