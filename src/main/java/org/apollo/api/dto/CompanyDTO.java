package org.apollo.api.dto;

public record CompanyDTO(Long id, String name, String cnpj, String contactEmail, String description, boolean active) {
}
