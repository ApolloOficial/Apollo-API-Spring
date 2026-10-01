package org.apollo.api.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AuthenticatedUser implements UserDetails {
    private final UUID userId;
    private final String fullName;
    private final Long companyId;
    private final UUID companyUnitId;
    private final String userType;
    private final String email;
    private final String password;
    private final boolean active;
    private final String roleName;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(AuthUser user) {
        this.userId = user.getUserId();
        this.fullName = user.getFullName();
        this.companyId = user.getCompanyId();
        this.companyUnitId = user.getCompanyUnitId();
        this.userType = user.getUserType();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.active = user.isActive();
        this.roleName = canonicalRole(user.getRole().getName());
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    private String canonicalRole(String role) {
        String normalized = Normalizer.normalize(role, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase();
        return switch (normalized) {
            case "ADMINISTRADOR", "ADMINISTRATOR" -> "ADMINISTRATOR";
            case "OPERADOR", "OPERATOR" -> "OPERATOR";
            case "ANALISTA", "ANALYST" -> "ANALYST";
            // BUG FIX: this used to return "GERENTE" (the Portuguese DB value) while
            // TenantContext.roleRank() and SecurityConfig both expect "MANAGER". That
            // mismatch made every manager fall into roleRank()'s default case (rank 0),
            // silently breaking requireCanAssign()/requireRoleAtLeast() for the whole
            // MANAGER role (e.g. a manager could not be allowed to assign a TECHNICIAN,
            // since 1 > 0). Standardized on the English name everywhere.
            case "GERENTE", "MANAGER" -> "MANAGER";
            case "TECNICO", "TECHNICIAN" -> "TECHNICIAN";
            case "SUPER_ADMIN" -> "SUPER_ADMIN";
            default -> throw new IllegalArgumentException("Unrecognized role");
        };
    }

    public UUID getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public Long getCompanyId() { return companyId; }
    public UUID getCompanyUnitId() { return companyUnitId; }
    public String getUserType() { return userType; }
    public String getEmail() { return email; }
    public String getRoleName() { return roleName; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return active; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}