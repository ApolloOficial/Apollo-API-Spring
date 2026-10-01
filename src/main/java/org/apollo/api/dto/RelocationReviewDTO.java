package org.apollo.api.dto;

import jakarta.validation.constraints.NotNull;
import org.apollo.api.enums.RelocationStatusEnum;

// Decisao do gerente: APROVADA ou REJEITADA.
public record RelocationReviewDTO(@NotNull(message = "Status is required") RelocationStatusEnum status) {
}
