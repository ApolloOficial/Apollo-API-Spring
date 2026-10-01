package org.apollo.api.service;

import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.HealthHistoryDTO;
import org.apollo.api.dto.MeasurementDTO;
import org.apollo.api.dto.RelocationCandidateDTO;
import org.apollo.api.dto.StringDTO;
import org.apollo.api.exception.ResourceNotFoundException;
import org.apollo.api.model.StringOverview;
import org.apollo.api.repository.CompanyUnitRepository;
import org.apollo.api.repository.PanelStringRepository;
import org.apollo.api.repository.StringHealthHistoryRepository;
import org.apollo.api.repository.StringOverviewRepository;
import org.apollo.api.repository.StringPerformanceMeasurementRepository;
import org.apollo.api.security.TenantContext;
import org.apollo.api.util.DbProcedures;
import org.apollo.api.util.Scope;
import org.apollo.api.util.Specs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StringService {

    private final StringOverviewRepository stringOverviewRepository;
    private final PanelStringRepository panelStringRepository;
    private final StringPerformanceMeasurementRepository measurementRepository;
    private final StringHealthHistoryRepository healthHistoryRepository;
    private final CompanyUnitRepository companyUnitRepository;
    private final DbProcedures procedures;
    private final TenantContext tenantContext;

    @Transactional(readOnly = true)
    public Page<StringDTO> findAll(UUID companyUnitId, String status, String inverterCode, String search,
                                   BigDecimal maxHealth, Boolean withWarnings, Pageable pageable) {
        Specification<StringOverview> spec = Specification
                .where(Scope.<StringOverview>unitColumnInCompany("companyUnitId", companyId()))
                .and(Specs.<StringOverview>equalTo(s -> s.get("companyUnitId"), companyUnitId))
                .and(Specs.<StringOverview>equalTo(s -> s.get("status"),
                        status == null || status.isBlank() ? null : status.trim().toUpperCase()))
                .and(Specs.<StringOverview>contains(s -> s.get("inverterCode"), inverterCode))
                .and(matchesCodeOrInvoice(search))
                .and(maxHealth == null ? null : healthBelow(maxHealth))
                .and(Boolean.TRUE.equals(withWarnings) ? hasWarnings() : null);
        return stringOverviewRepository.findAll(spec, pageable).map(StringDTO::of);
    }

    @Transactional(readOnly = true)
    public StringDTO findById(UUID id) {
        Specification<StringOverview> spec = Specification
                .where(Scope.<StringOverview>unitColumnInCompany("companyUnitId", companyId()))
                .and(Specs.<StringOverview>equalTo(s -> s.get("stringId"), id));
        return stringOverviewRepository.findOne(spec)
                .map(StringDTO::of)
                .orElseThrow(() -> new ResourceNotFoundException("String not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<MeasurementDTO> measurements(UUID stringId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        requireOwnString(stringId);
        LocalDateTime end = to != null ? to : DbProcedures.now();
        LocalDateTime start = from != null ? from : end.minusDays(1);
        return measurementRepository.findByStringIdAndMeasuredAtBetween(stringId, start, end, pageable)
                .map(MeasurementDTO::of);
    }

    @Transactional(readOnly = true)
    public Page<HealthHistoryDTO> healthHistory(UUID stringId, Pageable pageable) {
        requireOwnString(stringId);
        return healthHistoryRepository.findByStringId(stringId, pageable).map(HealthHistoryDTO::of);
    }

    @Transactional(readOnly = true)
    public List<RelocationCandidateDTO> relocationCandidates(BigDecimal threshold) {
        return procedures.relocationCandidates(companyId(), threshold != null ? threshold : BigDecimal.valueOf(70));
    }

    /** Recalcula o indice de saude das strings da filial (ou de toda a empresa se nao informar a filial). */
    public void recalculateHealth(UUID companyUnitId, Integer windowDays, BigDecimal threshold) {
        if (companyUnitId != null) {
            companyUnitRepository.findByIdAndCompanyId(companyUnitId, companyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + companyUnitId));
            procedures.recalculateHealth(companyUnitId, windowDays != null ? windowDays : 7,
                    threshold != null ? threshold : BigDecimal.valueOf(70));
            return;
        }
        // Sem filial: recalcula cada filial da empresa (a procedure sozinha rodaria no sistema todo).
        companyUnitRepository.findAll(Specs.<org.apollo.api.model.CompanyUnit>equalTo(
                        u -> u.get("company").get("id"), companyId()))
                .forEach(unit -> procedures.recalculateHealth(unit.getId(), windowDays != null ? windowDays : 7,
                        threshold != null ? threshold : BigDecimal.valueOf(70)));
    }

    private void requireOwnString(UUID stringId) {
        panelStringRepository.findByIdAndInverterCompanyUnitCompanyId(stringId, companyId())
                .orElseThrow(() -> new ResourceNotFoundException("String not found: " + stringId));
    }

    private Specification<StringOverview> matchesCodeOrInvoice(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), pattern),
                cb.like(cb.lower(root.get("invoiceNumber")), pattern));
    }

    private Specification<StringOverview> healthBelow(BigDecimal maxHealth) {
        return (root, query, cb) -> cb.lessThan(root.<BigDecimal>get("healthScore"), maxHealth);
    }

    private Specification<StringOverview> hasWarnings() {
        return (root, query, cb) -> cb.greaterThan(root.<Long>get("activeWarnings"), 0L);
    }

    private Long companyId() {
        return tenantContext.getCompanyId();
    }
}
