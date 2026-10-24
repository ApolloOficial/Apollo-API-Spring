package org.apollo.api.sungrow;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Planta real: usa o token guardado e chama a API da Sungrow. Se a Sungrow recusar o token, renova uma vez e
 * repete. Sem autorizacao ainda, falha com NOT_AUTHORIZED (o job avisa e tenta de novo na proxima rodada).
 */
@Component
@ConditionalOnSungrowLive
public class SungrowLiveSource implements SungrowRealTimeSource {

    private final SungrowTokenService tokens;
    private final SungrowClient client;

    public SungrowLiveSource(SungrowTokenService tokens, SungrowClient client) {
        this.tokens = tokens;
        this.client = client;
    }

    @Override
    public List<Map<String, Object>> realTime(List<String> serialNumbers, List<String> pointIds) {
        String token = tokens.accessToken()
                .orElseThrow(() -> new SungrowApiException(SungrowApiException.NOT_AUTHORIZED,
                        "no stored Sungrow authorization"));
        try {
            return client.realTime(token, serialNumbers, pointIds);
        } catch (SungrowApiException exception) {
            if (!exception.isInvalidToken()) {
                throw exception;
            }
            return client.realTime(tokens.refresh(token), serialNumbers, pointIds);
        }
    }
}
