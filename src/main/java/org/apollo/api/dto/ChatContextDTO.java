package org.apollo.api.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatContextDTO {

    @Size(max = 120, message = "Component must have at most 120 characters")
    private String component;

    @Size(max = 120, message = "Manufacturer must have at most 120 characters")
    private String manufacturer;

    @Size(max = 120, message = "Model must have at most 120 characters")
    private String model;

    @Size(max = 160, message = "Serial must have at most 160 characters")
    private String serial;

    @Size(max = 160, message = "Barcode must have at most 160 characters")
    private String barcode;

    @Size(max = 600, message = "Symptom must have at most 600 characters")
    private String symptom;

    @Size(max = 600, message = "Alert must have at most 600 characters")
    private String alert;

    @Size(max = 1000, message = "Measurements must have at most 1000 characters")
    private String measurements;

    @Size(max = 600, message = "Environmental condition must have at most 600 characters")
    private String environmentalCondition;

    @Size(max = 1000, message = "Performed procedure must have at most 1000 characters")
    private String performedProcedure;
}
