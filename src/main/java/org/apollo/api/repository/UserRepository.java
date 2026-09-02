package org.apollo.api.repository;

import org.apollo.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByCompanyId(Long companyId);
    Optional<User> findByIdAndCompanyId(Long id, Long companyId);
    Optional<User> findByCompanyIdAndEmail(Long companyId, String email);
    Optional<User> findByCompanyIdAndCpf(Long companyId, String cpf);
}
