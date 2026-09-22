package org.apollo.api.security;

public record JwtAuthenticationData(String userId, Long companyId, String userType, String email) {
}
