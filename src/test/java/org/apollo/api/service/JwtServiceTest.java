package org.apollo.api.service;

import org.apollo.api.model.Roles;
import org.apollo.api.security.AuthUser;
import org.apollo.api.security.AuthenticatedUser;
import org.apollo.api.security.JwtAuthenticationData;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Test
    void shouldIncludeRoleAndCompanyUnitClaimsInToken() {
        JwtService jwtService = new JwtService(
                Base64.getEncoder().encodeToString(new byte[32]),
                60_000,
                "apollo-api",
                "apollo-api-client"
        );
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        UUID companyUnitId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.getUserId()).thenReturn(userId);
        when(authUser.getCompanyId()).thenReturn(22L);
        when(authUser.getCompanyUnitId()).thenReturn(companyUnitId);
        when(authUser.getEmail()).thenReturn("admin@apollo.local");
        when(authUser.getRole()).thenReturn(new Roles(1L, "ADMINISTRATOR", null));

        String token = jwtService.generateToken(new AuthenticatedUser(authUser));
        JwtAuthenticationData identity = jwtService.extractAuthentication(token);

        assertEquals(userId, identity.userId());
        assertEquals(22L, identity.companyId());
        assertEquals("ADMINISTRATOR", identity.role());
        assertEquals("admin@apollo.local", identity.email());
        assertEquals(companyUnitId, identity.companyUnitId());
    }
}