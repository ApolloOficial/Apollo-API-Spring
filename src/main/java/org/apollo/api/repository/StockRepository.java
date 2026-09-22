package org.apollo.api.repository;

import org.apollo.api.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findAllByCompanyUnitCompanyId(Long companyId);
    Optional<Stock> findByIdAndCompanyUnitCompanyId(Long id, Long companyId);
    boolean existsByCompanyUnitIdAndSku(java.util.UUID companyUnitId, String sku);
}