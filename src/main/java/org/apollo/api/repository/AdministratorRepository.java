package org.apollo.api.repository;

import org.apollo.api.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    List<Administrator> findAllByCompanyId(Long companyId);
    Optional<Administrator> findByIdAndCompanyId(Long id, Long companyId);
    Optional<Administrator> findByCompanyIdAndEmail(Long companyId, String email);
    Optional<Administrator> findByCompanyIdAndCpf(Long companyId, String cpf);
}
