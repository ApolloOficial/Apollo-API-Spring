package org.apollo.api.repository;

import org.apollo.api.model.StringOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface StringOverviewRepository extends JpaRepository<StringOverview, UUID>, JpaSpecificationExecutor<StringOverview> {
}
