package org.apollo.api.util;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import java.util.function.Function;

public final class Specs {
    private Specs() {}

    /**
     * Filtro vazio (sempre verdadeiro). Spring Data JPA 4 nao aceita {@code null} em
     * {@code Specification.and(...)}, entao filtros ausentes devem usar este valor.
     */
    public static <T> Specification<T> none() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static <T> Specification<T> equalTo(Function<Root<T>, Path<?>> path, Object value) {
        if (value == null) return none();
        return (root, query, cb) -> cb.equal(path.apply(root), value);
    }

    public static <T> Specification<T> contains(Function<Root<T>, Path<String>> path, String text) {
        if (text == null || text.isBlank()) return none();
        String pattern = "%" + text.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(path.apply(root)), pattern);
    }
}