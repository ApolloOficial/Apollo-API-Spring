package org.apollo.api.dto;

public record MobileHomePendingRegistrationDTO(long batchId, String batchCode, int registered, int total) {
}
