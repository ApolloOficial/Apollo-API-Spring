package org.apollo.api.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.apollo.api.model.CompanyUnit;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Filtros de escopo (multi-tenant) reutilizaveis.
 * <p>
 * Uma filial pertence a uma empresa (company_unit.company_id). Quase tudo no sistema
 * chega na filial por algum caminho (inversor -> filial, string -> inversor -> filial...).
 * Aqui ficam os caminhos para nao repetir esse "encadeamento de joins" em cada servico.
 */
public final class Scope {

    private Scope() {
    }

    /** Entidades/views que tem uma coluna company_unit_id simples (ex.: StringOverview, PanelOverview). */
    public static <T> Specification<T> unitColumnInCompany(String unitAttribute, Long companyId) {
        return (root, query, cb) -> {
            Subquery<UUID> units = query.subquery(UUID.class);
            var unit = units.from(CompanyUnit.class);
            units.select(unit.get("id")).where(cb.equal(unit.get("company").get("id"), companyId));
            return root.get(unitAttribute).in(units);
        };
    }

    // ---- Alerta (Warning): aponta para uma string OU para uma placa -------------------------

    private static Path<Object> unitViaString(From<?, ?> warning) {
        Join<Object, Object> panelString = warning.join("panelString", JoinType.LEFT);
        Join<Object, Object> inverter = panelString.join("inverter", JoinType.LEFT);
        return inverter.get("companyUnit");
    }

    private static Path<Object> unitViaPanel(From<?, ?> warning) {
        Join<Object, Object> panel = warning.join("panel", JoinType.LEFT);
        Join<Object, Object> panelString = panel.join("panelString", JoinType.LEFT);
        Join<Object, Object> inverter = panelString.join("inverter", JoinType.LEFT);
        return inverter.get("companyUnit");
    }

    /** O alerta pertence a uma filial da empresa informada? */
    public static Predicate warningInCompany(CriteriaBuilder cb, From<?, ?> warning, Long companyId) {
        return cb.or(
                cb.equal(unitViaString(warning).get("company").get("id"), companyId),
                cb.equal(unitViaPanel(warning).get("company").get("id"), companyId));
    }

    /** O alerta pertence a esta filial? */
    public static Predicate warningInUnit(CriteriaBuilder cb, From<?, ?> warning, UUID unitId) {
        return cb.or(
                cb.equal(unitViaString(warning).get("id"), unitId),
                cb.equal(unitViaPanel(warning).get("id"), unitId));
    }
}
