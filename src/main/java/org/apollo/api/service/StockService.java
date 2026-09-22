package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.StockDTO;
import org.apollo.api.exception.BusinessRuleException;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.CompanyUnit;
import org.apollo.api.model.Stock;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.StockRepository;
import org.apollo.api.security.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final TenantContext tenantContext;

    public List<StockDTO> findAll() {
        return stockRepository.findAllByCompanyUnitCompanyId(companyId()).stream().map(this::toDTO).toList();
    }

    public StockDTO findById(Long id) {
        return toDTO(findStock(id));
    }

    public StockDTO create(StockDTO dto) {
        CompanyUnit unit = findUnit(dto.getCompanyUnitId());
        if (stockRepository.existsByCompanyUnitIdAndSku(unit.getId(), dto.getSku())) {
            throw new BusinessRuleException("Já existe item com este SKU nesta unidade");
        }
        Stock stock = new Stock();
        stock.setCompanyUnit(unit);
        updateFields(stock, dto);
        return toDTO(stockRepository.save(stock));
    }

    public StockDTO update(Long id, StockDTO dto) {
        Stock stock = findStock(id);
        updateFields(stock, dto);
        stock.setUpdatedAt(LocalDateTime.now());
        return toDTO(stockRepository.save(stock));
    }

    public void delete(Long id) {
        stockRepository.delete(findStock(id));
    }

    private Stock findStock(Long id) {
        return stockRepository.findByIdAndCompanyUnitCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque não encontrado: " + id));
    }

    private CompanyUnit findUnit(UUID id) {
        return companyUnitRepository.findByIdAndCompanyId(id, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada: " + id));
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }

    private void updateFields(Stock stock, StockDTO dto) {
        stock.setSku(dto.getSku());
        stock.setPartName(dto.getPartName());
        stock.setPartManufacturer(dto.getPartManufacturer());
        stock.setAvailableQtt(dto.getAvailableQtt());
        stock.setMinimumQtt(dto.getMinimumQtt());
        stock.setUnitCost(dto.getUnitCost());
    }

    private StockDTO toDTO(Stock stock) {
        return new StockDTO(
                stock.getId(),
                stock.getCompanyUnit().getId(),
                stock.getSku(),
                stock.getPartName(),
                stock.getPartManufacturer(),
                stock.getAvailableQtt(),
                stock.getMinimumQtt(),
                stock.getUnitCost(),
                stock.getUpdatedAt()
        );
    }
}