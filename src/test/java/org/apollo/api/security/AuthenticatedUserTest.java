package org.apollo.api.security;

import org.apollo.api.model.Roles;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticatedUserTest {

    private AuthenticatedUser userWithDatabaseRole(String databaseRoleName) {
        AuthUser authUser = mock(AuthUser.class);
        when(authUser.getUserId()).thenReturn(UUID.randomUUID());
        when(authUser.getCompanyId()).thenReturn(1L);
        when(authUser.getEmail()).thenReturn("user@apollo.local");
        when(authUser.isActive()).thenReturn(true);
        when(authUser.getRole()).thenReturn(new Roles(1L, databaseRoleName, null));
        return new AuthenticatedUser(authUser);
    }

    // Regression test: the DB stores "GERENTE", but TenantContext.roleRank() only knows
    // "MANAGER". The canonical name must be MANAGER so managers get the right rank.
    @Test
    void shouldMapDatabaseRoleGerenteToManager() {
        AuthenticatedUser user = userWithDatabaseRole("GERENTE");
        assertEquals("MANAGER", user.getRoleName());
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }

    @Test
    void shouldMapAllDatabaseRolesToEnglishNames() {
        assertEquals("ADMINISTRATOR", userWithDatabaseRole("ADMINISTRADOR").getRoleName());
        assertEquals("ANALYST", userWithDatabaseRole("ANALISTA").getRoleName());
        assertEquals("OPERATOR", userWithDatabaseRole("OPERADOR").getRoleName());
        assertEquals("TECHNICIAN", userWithDatabaseRole("TECNICO").getRoleName());
    }
}
