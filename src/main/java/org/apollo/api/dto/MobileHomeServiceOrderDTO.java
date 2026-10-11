package org.apollo.api.dto;

public record MobileHomeServiceOrderDTO(long id, String code, String assetCode, String status, String priority,
                                        String dueDate) {
}
