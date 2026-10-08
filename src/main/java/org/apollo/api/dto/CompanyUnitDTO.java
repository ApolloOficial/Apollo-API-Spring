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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long segmentId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String segmentName;

    @NotNull(message = "Address is required")
    @Valid
    private AddressDTO address;

    @NotBlank(message = "Name is required")
    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate createdAt;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone must contain 10 or 11 digits")
    private String phone;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String cnpj;

    private UUID responsibleEmployeeId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String responsibleEmployeeName;

    @Email(message = "Invalid contact email")
    private String contactEmail;

    private String contactPhone;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal kwpTotal;

    private Boolean active = true;

    // So preenchidos no detalhe (GET /company-units/{id}).
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long activeInvertersCount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long openServiceOrdersCount;

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
