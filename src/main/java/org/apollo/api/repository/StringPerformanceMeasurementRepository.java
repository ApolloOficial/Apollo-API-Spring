package org.apollo.api.repository;

import org.apollo.api.model.StringPerformanceMeasurement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface StringPerformanceMeasurementRepository extends JpaRepository<StringPerformanceMeasurement, Long> {

    Page<StringPerformanceMeasurement> findByStringIdAndMeasuredAtBetween(
            UUID stringId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
