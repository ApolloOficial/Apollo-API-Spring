package org.apollo.api.sungrow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Resposta de /apiManage/token e /apiManage/refreshToken. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SungrowTokenResponse(@JsonProperty("result_code") String resultCode,
                                   @JsonProperty("result_msg") String resultMsg,
                                   String error,
                                   @JsonProperty("error_description") String errorDescription,
                                   @JsonProperty("result_data") TokenData resultData) implements SungrowEnvelope {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TokenData(@JsonProperty("access_token") String accessToken,
                            @JsonProperty("refresh_token") String refreshToken,
                            @JsonProperty("expires_in") Long expiresIn,
                            @JsonProperty("auth_ps_list") List<Object> authPsList,
                            // So na renovacao: 1 = sucesso; -2 = refresh token expirado; 5 = invalido.
                            Object code) {
    }
}
