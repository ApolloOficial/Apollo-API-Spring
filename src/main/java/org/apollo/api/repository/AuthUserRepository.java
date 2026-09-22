package org.apollo.api.repository;

import org.apollo.api.security.AuthUser;
import org.apollo.api.security.AuthUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, AuthUserId> {

    @Query("""
            SELECT authUser FROM AuthUser authUser
            JOIN FETCH authUser.role
            WHERE authUser.companyId = :companyId
              AND authUser.email = :email
              AND authUser.active = true
            """)
    List<AuthUser> findActiveByCompanyIdANDEmail(
            @Param("companyId") Long companyId,
            @Param("email") String email
    );

    @Query("""
            SELECT authUser FROM AuthUser authUser
            JOIN FETCH authUser.role
            WHERE authUser.userId = :userId
              AND authUser.companyId = :companyId
              AND authUser.userType = :userType
              AND authUser.email = :email
              AND authUser.active = true
            """)
    Optional<AuthUser> findActiveByIdentity(
            @Param("userId") String userId,
            @Param("companyId") Long companyId,
            @Param("userType") String userType,
            @Param("email") String email
    );
}
