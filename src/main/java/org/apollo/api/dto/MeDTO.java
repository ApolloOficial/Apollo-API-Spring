package org.apollo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class MeDTO {
    private UUID userId;
    private String fullName;
    private String email;
    private String role;
    private Long companyId;
    private String companyName;
    private UUID companyUnitId;
    private String companyUnitName;
}