package org.apollo.api.security;

public record JwtAuthenticationData(Long userId, Long companyId, String userType, String email) {
}
