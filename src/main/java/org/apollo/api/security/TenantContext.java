package org.apollo.api.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantContext {

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AccessDeniedException("Invalid authenticated user");
        }
        return user;
    }

    public Long getCompanyId() {
        return currentUser().getCompanyId();
    }

    public UUID getUserId() {
        return currentUser().getUserId();
    }

    public UUID getCompanyUnitId() {
        return currentUser().getCompanyUnitId();
    }

    public String getUserType() {
        return currentUser().getUserType();
    }

    public String getRoleName() {
        return currentUser().getRoleName();
    }

    public boolean isPlatformAdmin() {
        return "SUPER_ADMIN".equals(getRoleName());
    }

    public void requirePlatformAdmin() {
        if (!isPlatformAdmin()) {
            throw new AccessDeniedException("Operation allowed only for platform administrators");
        }
    }

    public void requireRoleAtLeast(String requiredRole) {
        if (roleRank(getRoleName()) < roleRank(requiredRole)) {
            throw new AccessDeniedException("Insufficient permission for this operation");
        }
    }

    public void requireCanAssign(String targetRoleName) {
        // O nome vem do banco ("Administrador", "Gerente"...): normaliza para o nome
        // canonico antes de comparar, senao qualquer cargo em portugues cairia no rank 0.
        String targetRole = canonicalRole(targetRoleName);
        if ("SUPER_ADMIN".equals(targetRole) && !isPlatformAdmin()) {
            throw new AccessDeniedException("Assigning the platform profile is not allowed");
        }
        if (roleRank(targetRole) > roleRank(getRoleName())) {
            throw new AccessDeniedException("Assigning a role higher than your own is not allowed");
        }
    }

    private String canonicalRole(String role) {
        if (role == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(role, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase();
        return switch (normalized) {
            case "ADMINISTRADOR", "ADMINISTRATOR" -> "ADMINISTRATOR";
            case "GERENTE", "MANAGER" -> "MANAGER";
            case "OPERADOR", "OPERATOR" -> "OPERATOR";
            case "ANALISTA", "ANALYST" -> "ANALYST";
            case "TECNICO", "TECHNICIAN" -> "TECHNICIAN";
            default -> normalized;
        };
    }

    private int roleRank(String role) {
        // A ordem reflete a hierarquia de negocio: o Gerente de Filial é o perfil
        // mais alto do app Web e precisa ficar ACIMA de Operador/Tecnico/Analista
        // para poder cadastra-los (requireCanAssign). Antes MANAGER=2 ficava abaixo
        // de OPERATOR=4, o que fazia o gerente NAO conseguir criar um operador
        // ("Assigning a role higher than your own is not allowed").
        return switch (role) {
            case "SUPER_ADMIN" -> 6;   // plataforma (Apollo)
            case "ADMINISTRATOR" -> 5; // Dev / setup de tenants
            case "MANAGER" -> 4;       // Gerente de Filial (topo do Web)
            case "OPERATOR" -> 3;
            case "ANALYST" -> 2;
            case "TECHNICIAN" -> 1;
            default -> 0;
        };
    }
}