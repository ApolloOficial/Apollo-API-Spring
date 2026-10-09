package org.apollo.api.dto;

public record ProfileDTO(String fullName, String email, String companyName, String unitName, String role,
                         String roleLabel, boolean hasPhoto, String photoUrl) {
}
