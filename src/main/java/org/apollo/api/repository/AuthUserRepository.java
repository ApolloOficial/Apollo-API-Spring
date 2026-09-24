package org.apollo.api.repository;

import org.apollo.api.security.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {

    @Query("""
            SELECT authUser FROM AuthUser authUser
            JOIN FETCH authUser.role
            WHERE LOWER(authUser.email) = LOWER(:email)
              AND authUser.active = true
            """)
    List<AuthUser> findActiveByEmail(@Param("email") String email);

    @Query("""
            SELECT authUser FROM AuthUser authUser
            JOIN FETCH authUser.role
            WHERE authUser.userId = :userId
              AND authUser.companyId = :companyId
              AND authUser.email = :email
              AND authUser.active = true
            """)
    Optional<AuthUser> findActiveByIdentity(
            @Param("userId") UUID userId,
            @Param("companyId") Long companyId,
            @Param("email") String email
    );
}