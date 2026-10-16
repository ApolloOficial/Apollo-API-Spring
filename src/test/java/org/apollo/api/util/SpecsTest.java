package org.apollo.api.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class SpecsTest {

    @Test
    void absentFiltersAreNeverNull() {
        assertNotNull(Specs.<Object>equalTo(r -> r.get("x"), null));
        assertNotNull(Specs.<Object>contains(r -> r.get("x"), "  "));
        assertNotNull(Specs.<Object>none());
    }

    @Test
    void absentFiltersComposeWithAnd() {
        assertDoesNotThrow(() -> Specification.<Object>where(Specs.<Object>none())
                .and(Specs.<Object>equalTo(r -> r.get("x"), null))
                .and(Specs.<Object>contains(r -> r.get("y"), null)));
    }
}
