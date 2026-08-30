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
            select authUser from AuthUser authUser
            join fetch authUser.role
            where authUser.companyId = :companyId
              and authUser.email = :email
              and authUser.active = true
            """)
    List<AuthUser> findActiveByCompanyIdAndEmail(
            @Param("companyId") Long companyId,
            @Param("email") String email
    );

    @Query("""
            select authUser from AuthUser authUser
            join fetch authUser.role
            where authUser.userId = :userId
              and authUser.companyId = :companyId
              and authUser.userType = :userType
              and authUser.email = :email
              and authUser.active = true
            """)
    Optional<AuthUser> findActiveByIdentity(
            @Param("userId") Long userId,
            @Param("companyId") Long companyId,
            @Param("userType") String userType,
            @Param("email") String email
    );
}
