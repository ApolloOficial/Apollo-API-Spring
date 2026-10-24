package org.apollo.api.sungrow;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente da API aberta da Sungrow (iSolarCloud). Todas as chamadas sao POST com JSON e levam
 * os headers x-access-key (e Authorization: Bearer, quando ha token) e o appkey no corpo.
 * Nunca registra em log tokens, appkey ou access key.
 */
@Component
@ConditionalOnSungrowLive
public class SungrowClient {

    private static final Logger log = LoggerFactory.getLogger(SungrowClient.class);
    private static final MediaType JSON_UTF8 = new MediaType("application", "json", StandardCharsets.UTF_8);
    private static final String SUCCESS = "1";
    /** device_type 1 = inversor (Appendix 1 da API). */
    private static final int DEVICE_TYPE_INVERTER = 1;

    private final RestClient restClient;
    private final SungrowProperties properties;

    public SungrowClient(@Qualifier("sungrowRestClient") RestClient restClient, SungrowProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /** Troca o codigo de autorizacao (devolvido ao redirect_uri) por access/refresh token. */
    public SungrowTokenResponse.TokenData exchangeCode(String code) {
        Map<String, Object> body = baseBody();
        body.put("code", code);
        body.put("grant_type", "authorization_code");
        body.put("redirect_uri", properties.getRedirectUri());
        SungrowTokenResponse response = post(properties.getTokenPath(), null, body, SungrowTokenResponse.class);
        return tokenData(response);
    }

    /** Renova o token. A Sungrow troca tambem o refresh token: o novo precisa ser salvo. */
    public SungrowTokenResponse.TokenData refresh(String refreshToken) {
        Map<String, Object> body = baseBody();
        body.put("refresh_token", refreshToken);
        SungrowTokenResponse response = post(properties.getRefreshPath(), null, body, SungrowTokenResponse.class);
        SungrowTokenResponse.TokenData data = tokenData(response);
        // Na renovacao a Sungrow tambem informa result_data.code (1 = sucesso).
        if (data.code() != null && !SUCCESS.equals(String.valueOf(data.code()))) {
            throw new SungrowApiException("REFRESH_" + data.code(), "refresh token rejected");
        }
        return data;
    }

    /**
     * Dados em tempo real (menor unidade: W, V, A) dos inversores pelos numeros de serie.
     * Devolve um mapa por dispositivo com device_sn, device_time, dev_fault_status e p{id}.
     */
    public List<Map<String, Object>> realTime(String accessToken, List<String> serialNumbers, List<String> pointIds) {
        Map<String, Object> body = baseBody();
        body.put("sn_list", serialNumbers);
        body.put("point_id_list", pointIds);
        body.put("device_type", DEVICE_TYPE_INVERTER);
        SungrowRealTimeResponse response = post(properties.getRealTimePath(), accessToken, body,
                SungrowRealTimeResponse.class);
        check(response);
        if (response.resultData() == null || response.resultData().devicePointList() == null) {
            return List.of();
        }
        return response.resultData().devicePointList();
    }

    private Map<String, Object> baseBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("appkey", properties.getAppkey());
        body.put("lang", properties.getLang());
        return body;
    }

    private <R extends SungrowEnvelope> R post(String path, String accessToken, Map<String, Object> body,
                                               Class<R> type) {
        try {
            RestClient.RequestBodySpec request = restClient.post()
                    .uri(path)
                    .contentType(JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("x-access-key", properties.getAccessKey());
            if (accessToken != null) {
                request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            }
            return request.body(body).exchange((req, res) -> {
                R parsed = null;
                try {
                    parsed = res.bodyTo(type);
                } catch (RestClientException exception) {
                    log.warn("Sungrow answered HTTP {} with an unreadable body", res.getStatusCode().value());
                }
                if (parsed == null) {
                    throw new SungrowApiException("HTTP_" + res.getStatusCode().value(),
                            "empty or unreadable response");
                }
                return parsed;
            });
        } catch (RestClientException exception) {
            log.warn("Sungrow call to {} failed: {}", path, exception.getMessage());
            throw new SungrowApiException("IO_ERROR", "could not reach Sungrow");
        }
    }

    private static SungrowTokenResponse.TokenData tokenData(SungrowTokenResponse response) {
        check(response);
        SungrowTokenResponse.TokenData data = response.resultData();
        if (data == null || isBlank(data.accessToken()) || isBlank(data.refreshToken())) {
            throw new SungrowApiException("INVALID_RESPONSE", "token response without access/refresh token");
        }
        return data;
    }

    private static void check(SungrowEnvelope response) {
        if (response.error() != null) {
            throw new SungrowApiException(response.error(), response.errorDescription());
        }
        if (!SUCCESS.equals(response.resultCode())) {
            throw new SungrowApiException(String.valueOf(response.resultCode()), response.resultMsg());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
