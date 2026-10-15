package org.apollo.api.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    // codigo estavel para o cliente decidir sem depender do texto (ex.: NOT_FOUND, CONFLICT)
    private String code;
    // id para cruzar a resposta com o log do servidor
    private String traceId;

    public ErrorResponse(int status, String message) {
        this(status, message, null, null);
    }
}
