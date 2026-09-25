package org.apollo.api.repository.Impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.apollo.api.dto.SearchResultDTO;
import org.apollo.api.repository.SearchRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SearchRepositoryImpl implements SearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SearchResultDTO> searchEmployees(String search, Long companyId, int limit) {

        StringBuilder jpql = new StringBuilder("""
            SELECT new org.apollo.api.dto.SearchResultDTO(
                e.id,
                e.fullName,
                'Funcionário'
            )
            FROM Employee e
            JOIN CompanyUnit cu ON cu.id = e.companyUnitId
            WHERE 1 = 1
            """);

        Map<String, Object> params = new HashMap<>();

        jpql.append("""
            AND cu.company.id = :companyId
            """);
        params.put("companyId", companyId);

        jpql.append("""
            AND LOWER(e.fullName) LIKE LOWER(:search)
            """);
        params.put("search", "%" + search + "%");

        jpql.append(" ORDER BY e.fullName ASC");

        return runQuery(jpql.toString(), params, limit);
    }

    @Override
    public List<SearchResultDTO> searchCompanyUnits(String search, Long companyId, int limit) {

        StringBuilder jpql = new StringBuilder("""
                SELECT new org.apollo.api.dto.SearchResultDTO(
                    cu.id,
                    cu.name,
                    'Filial'
                )
                FROM CompanyUnit cu
                WHERE 1 = 1
                """);

        Map<String, Object> params = new HashMap<>();

        jpql.append("""
                AND cu.company.id = :companyId
                """);
        params.put("companyId", companyId);

        jpql.append("""
                AND LOWER(cu.name) LIKE LOWER(:search)
                """);
        params.put("search", "%" + search + "%");

        jpql.append(" ORDER BY cu.name ASC");

        return runQuery(jpql.toString(), params, limit);
    }

    @Override
    public List<SearchResultDTO> searchBatches(String search, Long companyId, int limit) {

        StringBuilder jpql = new StringBuilder("""
                SELECT new org.apollo.api.dto.SearchResultDTO(
                    b.id,
                    b.model,
                    b.manufacturer
                )
                FROM Batch b
                WHERE 1 = 1
                """);

        Map<String, Object> params = new HashMap<>();

        jpql.append("""
                AND b.companyUnit.company.id = :companyId
                """);
        params.put("companyId", companyId);

        jpql.append("""
                AND (
                    LOWER(b.model) LIKE LOWER(:search)
                    OR LOWER(b.manufacturer) LIKE LOWER(:search)
                    OR LOWER(b.billNumber) LIKE LOWER(:search)
                )
                """);
        params.put("search", "%" + search + "%");

        jpql.append(" ORDER BY b.createdAt DESC");

        return runQuery(jpql.toString(), params, limit);
    }

    @Override
    public List<SearchResultDTO> searchInternalRelocations(String search, Long companyId, int limit) {

        StringBuilder jpql = new StringBuilder("""
                SELECT new org.apollo.api.dto.SearchResultDTO(
                    r.id,
                    r.justification,
                    'Realocação interna'
                )
                FROM SuggestedInternalRelocation r
                WHERE 1 = 1
                """);

        Map<String, Object> params = new HashMap<>();

        jpql.append("""
                AND r.batch.companyUnit.company.id = :companyId
                """);
        params.put("companyId", companyId);

        jpql.append("""
                AND LOWER(r.justification) LIKE LOWER(:search)
                """);
        params.put("search", "%" + search + "%");

        jpql.append(" ORDER BY r.id DESC");

        return runQuery(jpql.toString(), params, limit);
    }

    @Override
    public List<SearchResultDTO> searchExternalRelocations(String search, Long companyId, int limit) {

        StringBuilder jpql = new StringBuilder("""
                SELECT new org.apollo.api.dto.SearchResultDTO(
                    r.id,
                    r.justification,
                    'Realocação externa'
                )
                FROM SuggestedExternalRelocation r
                WHERE 1 = 1
                """);

        Map<String, Object> params = new HashMap<>();

        jpql.append("""
                AND r.panel.batch.companyUnit.company.id = :companyId
                """);
        params.put("companyId", companyId);

        jpql.append("""
                AND LOWER(r.justification) LIKE LOWER(:search)
                """);
        params.put("search", "%" + search + "%");

        jpql.append(" ORDER BY r.id DESC");

        return runQuery(jpql.toString(), params, limit);
    }

    private List<SearchResultDTO> runQuery(String jpql, Map<String, Object> params, int limit) {
        TypedQuery<SearchResultDTO> query = entityManager.createQuery(jpql, SearchResultDTO.class);
        params.forEach(query::setParameter);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}