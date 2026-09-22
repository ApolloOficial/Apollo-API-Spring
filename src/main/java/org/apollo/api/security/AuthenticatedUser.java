package org.apollo.api.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.Normalizer;
import java.util.Collection;
import java.util.List;

public class AuthenticatedUser implements UserDetails {
    private final String userId;
    private final Long companyId;
    private final String userType;
    private final String email;
    private final String password;
    private final boolean active;
    private final String roleName;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(AuthUser user) {
        this.userId = user.getUserId(); this.companyId = user.getCompanyId(); this.userType = user.getUserType();
        this.email = user.getEmail(); this.password = user.getPassword(); this.active = user.isActive();
        this.roleName = canonicalRole(user.getRole().getName());
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }
    private String canonicalRole(String role) {
        String normalized = Normalizer.normalize(role, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase();
        return switch (normalized) {
            case "ADMINISTRADOR", "ADMINISTRATOR" -> "ADMINISTRATOR";
            case "OPERADOR", "OPERATOR" -> "OPERATOR";
            case "ANALISTA", "ANALYST" -> "ANALYST";
            case "TECNICO", "TECHNICIAN" -> "TECHNICIAN";
            case "SUPER_ADMIN" -> "SUPER_ADMIN";
            default -> throw new IllegalArgumentException("Perfil não reconhecido");
        };
    }
    public String getUserId() { return userId; }
    public Long getCompanyId() { return companyId; }
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
