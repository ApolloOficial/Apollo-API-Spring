package org.apollo.api.repository;

import org.apollo.api.model.StringHealthHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StringHealthHistoryRepository extends JpaRepository<StringHealthHistory, Long> {

    Page<StringHealthHistory> findByStringId(UUID stringId, Pageable pageable);
}
