package org.apollo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private Long roleId;
    private String roleName;
    private String fullName;
    private String email;
    private String cpf;
}
