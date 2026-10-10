package org.apollo.api.dto;

public record NotificationPreferencesDTO(boolean predictiveMaintenance, boolean panelAlert, boolean emailSummary) {
}
