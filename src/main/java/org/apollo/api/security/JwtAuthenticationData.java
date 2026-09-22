package org.apollo.api.security;

import java.util.UUID;

public record JwtAuthenticationData(UUID userId, Long companyId, String email, String role, UUID companyUnitId) {
}