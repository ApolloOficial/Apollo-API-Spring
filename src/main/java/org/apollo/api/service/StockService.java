package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.StockCreateDTO;
import org.apollo.api.dto.StockDTO;
import org.apollo.api.dto.StockUpdateDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.model.Part;
import org.apollo.api.model.Stock;
import org.apollo.api.model.StockId;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.PartRepository;
import org.apollo.api.repository.StockRepository;
import org.apollo.api.security.TenantContext;
import org.apollo.api.util.DbProcedures;
import org.apollo.api.util.Specs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final StockRepository stockRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final PartRepository partRepository;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public Page<StockDTO> findAll(UUID companyUnitId, String search, Boolean belowMinimum, Pageable pageable) {
        Specification<Stock> spec = Specification
                .where(Specs.<Stock>equalTo(s -> s.get("companyUnit").get("company").get("id"), companyId()))
                .and(Specs.<Stock>equalTo(s -> s.get("companyUnit").get("id"), companyUnitId))
                .and(matchesPart(search))
                .and(Boolean.TRUE.equals(belowMinimum) ? belowMinimum() : null);
        return stockRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public StockDTO findById(UUID companyUnitId, Long partId) {
        return toDTO(findStock(companyUnitId, partId));
    }

    /** Cadastra uma peca no estoque da filial (a propria filial do usuario, se nao informar outra). */
    public StockDTO create(StockCreateDTO dto) {
        UUID unitId = dto.companyUnitId() != null ? dto.companyUnitId() : tenantContext.getCompanyUnitId();
        CompanyUnit unit = companyUnitRepository.findByIdAndCompanyId(unitId, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));
        Part part = partRepository.findById(dto.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found: " + dto.partId()));
        StockId id = new StockId(unit.getId(), part.getId());
        if (stockRepository.existsById(id)) {
            throw new BusinessRuleException("This part is already in the stock of the unit; update it instead");
        }
        Stock stock = new Stock();
        stock.setId(id);
        stock.setCompanyUnit(unit);
        stock.setPart(part);
        stock.setAvailableQtt(dto.availableQtt());
        stock.setMinimumQtt(dto.minimumQtt());
        stock.setUnitCost(dto.unitCost());
        stock.setUpdatedAt(DbProcedures.now());
        return toDTO(stockRepository.saveAndFlush(stock));
    }

    public StockDTO update(UUID companyUnitId, Long partId, StockUpdateDTO dto) {
        Stock stock = findStock(companyUnitId, partId);
        stock.setAvailableQtt(dto.availableQtt());
        stock.setMinimumQtt(dto.minimumQtt());
        stock.setUnitCost(dto.unitCost());
        stock.setUpdatedAt(DbProcedures.now());
        return toDTO(stockRepository.saveAndFlush(stock));
    }

    public void delete(UUID companyUnitId, Long partId) {
        stockRepository.delete(findStock(companyUnitId, partId));
        stockRepository.flush();
    }

    private Stock findStock(UUID companyUnitId, Long partId) {
        Stock stock = stockRepository.findById(new StockId(companyUnitId, partId))
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + partId));
        if (!stock.getCompanyUnit().getCompany().getId().equals(companyId())) {
            // Outra empresa: responde 404 para nao confirmar que existe.
            throw new ResourceNotFoundException("Stock item not found: " + partId);
        }
        return stock;
    }

    private Specification<Stock> matchesPart(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("part").get("name")), pattern),
                cb.like(cb.lower(root.get("part").get("sku")), pattern));
    }

    private Specification<Stock> belowMinimum() {
        return (root, query, cb) -> cb.lessThan(root.<Integer>get("availableQtt"), root.<Integer>get("minimumQtt"));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private StockDTO toDTO(Stock s) {
        return new StockDTO(s.getCompanyUnit().getId(), s.getCompanyUnit().getName(), s.getPart().getId(),
                s.getPart().getSku(), s.getPart().getName(), s.getPart().getManufacturer(),
                s.getAvailableQtt(), s.getMinimumQtt(), s.getUnitCost(), s.getUpdatedAt(),
                s.getAvailableQtt() < s.getMinimumQtt());
    }
}
