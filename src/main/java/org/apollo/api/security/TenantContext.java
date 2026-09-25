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
            throw new AccessDeniedException("Usuário autenticado inválido");
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
            throw new AccessDeniedException("Operação permitida somente para administrador de plataforma");
        }
    }

    public void requireRoleAtLeast(String requiredRole) {
        if (roleRank(getRoleName()) < roleRank(requiredRole)) {
            throw new AccessDeniedException("Permissão insuficiente para esta operação");
        }
    }

    public void requireCanAssign(String targetRole) {
        if ("SUPER_ADMIN".equals(targetRole) && !isPlatformAdmin()) {
            throw new AccessDeniedException("Não é permitido atribuir o perfil de plataforma");
        }
        if (roleRank(targetRole) > roleRank(getRoleName())) {
            throw new AccessDeniedException("Não é permitido atribuir um perfil superior ao seu");
        }
    }

    private int roleRank(String role) {
        return switch (role) {
            case "SUPER_ADMIN" -> 6;
            case "ADMINISTRATOR" -> 5;
            case "OPERATOR" -> 4;
            case "ANALYST" -> 3;
            case "MANAGER" -> 2;
            case "TECHNICIAN" -> 1;
            default -> 0;
        };
    }
}