package org.apollo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CompanyUnitDTO {

    private UUID id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long companyId;

    @NotNull(message = "Segmento é obrigatório")
    private Long segmentId;

    private String segmentName;

    @NotNull(message = "Endereço é obrigatório")
    @Valid
    private AddressDTO address;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate createdAt;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Telefone deve conter 10 ou 11 dígitos")
    private String phone;

    @Pattern(regexp = "^[0-9]{14,18}$", message = "CNPJ inválido")
    private String cnpj;

    private UUID responsibleEmployeeId;

    @Email(message = "Email de contato inválido")
    private String contactEmail;

    private String contactPhone;

    @DecimalMin(value = "0.0", message = "kWp total não pode ser negativo")
    private BigDecimal kwpTotal;

    private Boolean active = true;

    public CompanyUnitDTO(UUID id, Long companyId, Long segmentId, String segmentName, AddressDTO address,
                          String name, LocalDate createdAt, String email, String phone,
                          String cnpj, UUID responsibleEmployeeId, String contactEmail,
                          String contactPhone, BigDecimal kwpTotal, Boolean active) {
        this.id = id;
        this.companyId = companyId;
        this.segmentId = segmentId;
        this.segmentName = segmentName;
        this.address = address;
        this.name = name;
        this.createdAt = createdAt;
        this.email = email;
        this.phone = phone;
        this.cnpj = cnpj;
        this.responsibleEmployeeId = responsibleEmployeeId;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.kwpTotal = kwpTotal;
        this.active = active;
    }

    public CompanyUnitDTO(UUID id, Long companyId, Long segmentId, String segmentName, AddressDTO address,
                          String name, LocalDate createdAt, String email, String phone) {
        this(id, companyId, segmentId, segmentName, address, name, createdAt, email, phone,
                null, null, null, null, null, true);
    }
}
