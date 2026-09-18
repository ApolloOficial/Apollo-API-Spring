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

    // EMPLOYEES
    @Override
    public List<SearchResultDTO> searchEmployees(
            String search,
            Long companyId
    ) {

        StringBuilder jpql = new StringBuilder("""
                SELECT new org.apollo.api.dto.SearchResultDTO(
                    e.id,
                    e.name,
                    'Funcionário'
                )
                FROM Employee e
                WHERE 1 = 1
                """);

        Map<String, Object> params = new HashMap<>();

        jpql.append("""
                AND e.company.id = :companyId
                """);

        params.put("companyId", companyId);

        jpql.append("""
                AND LOWER(e.name) LIKE LOWER(:search)
                """);

        params.put("search", "%" + search + "%");

        jpql.append(" ORDER BY e.name ASC");

        TypedQuery<SearchResultDTO> query =
                entityManager.createQuery(
                        jpql.toString(),
                        SearchResultDTO.class
                );

        params.forEach(query::setParameter);

        query.setMaxResults(10);

        return query.getResultList();
    }

    // COMPANY UNITS
    @Override
    public List<SearchResultDTO> searchCompanyUnits(
            String search,
            Long companyId
    ) {

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

        TypedQuery<SearchResultDTO> query =
                entityManager.createQuery(
                        jpql.toString(),
                        SearchResultDTO.class
                );

        params.forEach(query::setParameter);

        query.setMaxResults(10);

        return query.getResultList();
    }

    // BATCHES
    @Override
    public List<SearchResultDTO> searchBatches(
            String search,
            Long companyId
    ) {

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
                AND b.company.id = :companyId
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

        jpql.append(" ORDER BY b.id DESC");

        TypedQuery<SearchResultDTO> query =
                entityManager.createQuery(
                        jpql.toString(),
                        SearchResultDTO.class
                );

        params.forEach(query::setParameter);

        query.setMaxResults(10);

        return query.getResultList();
    }

    // INTERNAL RELOCATIONS
    @Override
    public List<SearchResultDTO> searchInternalRelocations(
            String search,
            Long companyId
    ) {

        StringBuilder jpql = new StringBuilder("""
                SELECT new org.apollo.api.dto.SearchResultDTO(
                    r.id,
                    r.name,
                    'Realocação interna'
                )
                FROM SuggestedInternalRelocation r
                WHERE 1 = 1
                """);

        Map<String, Object> params = new HashMap<>();

        jpql.append("""
                AND r.company.id = :companyId
                """);

        params.put("companyId", companyId);

        jpql.append("""
                AND LOWER(r.name) LIKE LOWER(:search)
                """);

        params.put("search", "%" + search + "%");

        jpql.append(" ORDER BY r.id DESC");

        TypedQuery<SearchResultDTO> query =
                entityManager.createQuery(
                        jpql.toString(),
                        SearchResultDTO.class
                );

        params.forEach(query::setParameter);

        query.setMaxResults(10);

        return query.getResultList();
    }

    // EXTERNAL RELOCATIONS
    @Override
    public List<SearchResultDTO> searchExternalRelocations(
            String search,
            Long companyId
    ) {

        StringBuilder jpql = new StringBuilder("""
                SELECT new org.apollo.api.dto.SearchResultDTO(
                    r.id,
                    r.name,
                    'Realocação externa'
                )
                FROM SuggestedExternalRelocation r
                WHERE 1 = 1
                """);

        Map<String, Object> params = new HashMap<>();

        jpql.append("""
                AND r.company.id = :companyId
                """);

        params.put("companyId", companyId);

        jpql.append("""
                AND LOWER(r.name) LIKE LOWER(:search)
                """);

        params.put("search", "%" + search + "%");

        jpql.append(" ORDER BY r.id DESC");

        TypedQuery<SearchResultDTO> query =
                entityManager.createQuery(
                        jpql.toString(),
                        SearchResultDTO.class
                );

        params.forEach(query::setParameter);

        query.setMaxResults(10);

        return query.getResultList();
    }
}