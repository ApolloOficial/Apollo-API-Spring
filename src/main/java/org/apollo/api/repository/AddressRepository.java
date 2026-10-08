package org.apollo.api.repository;

import org.apollo.api.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Address nao tem company_id proprio: pertence ao tenant apenas atraves do
    // company_unit que o referencia. A raiz da query e Address (alias a) para que o
    // Sort do Pageable (ex.: "id") resolva sem ambiguidade; o escopo multi-tenant e
    // garantido pela subconsulta em company_unit -> company.
    @Query("""
            SELECT a FROM Address a
            WHERE a.id IN (
                SELECT cu.address.id FROM CompanyUnit cu
                WHERE cu.company.id = :companyId AND cu.address IS NOT NULL
            )
            """)
    Page<Address> findAllForCompany(@Param("companyId") Long companyId, Pageable pageable);
}