package org.apollo.api.util;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import java.util.function.Function;

public final class Specs {
    private Specs() {}

    public static <T> Specification<T> equalTo(Function<Root<T>, Path<?>> path, Object value) {
        if (value == null) return null;
        return (root, query, cb) -> cb.equal(path.apply(root), value);
    }

    public static <T> Specification<T> contains(Function<Root<T>, Path<String>> path, String text) {
        if (text == null || text.isBlank()) return null;
        String pattern = "%" + text.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(path.apply(root)), pattern);
    }
}