package org.apollo.api.repository;

import java.util.UUID;
import org.apollo.api.model.EmployeePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeePhotoRepository extends JpaRepository<EmployeePhoto, UUID> {
}
