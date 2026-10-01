package org.apollo.api.repository;

import org.apollo.api.model.Stock;
import org.apollo.api.model.StockId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockRepository extends JpaRepository<Stock, StockId>, JpaSpecificationExecutor<Stock> {

    @Override
    @EntityGraph(attributePaths = {"companyUnit", "part"})
    Page<Stock> findAll(Specification<Stock> spec, Pageable pageable);
}
