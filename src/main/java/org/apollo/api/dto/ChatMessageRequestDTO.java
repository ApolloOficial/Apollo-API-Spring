package org.apollo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDTO {

    @Pattern(regexp = "^[A-Za-z0-9_.:-]{8,128}$", message = "Session id must have 8 to 128 letters, digits or _ . : -")
    private String sessionId;

    @NotBlank(message = "Message is required")
    @Size(max = 4000, message = "Message must have at most 4000 characters")
    private String message;

    @Valid
    private ChatContextDTO context;
}
