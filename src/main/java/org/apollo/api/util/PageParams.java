package org.apollo.api.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageParams {
    private static final int MAX_SIZE = 100;
    private PageParams() {}

    public static Pageable of(int page, int size, Sort.Direction direction, String sortProperty) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_SIZE),
                Sort.by(direction, sortProperty));
    }
}