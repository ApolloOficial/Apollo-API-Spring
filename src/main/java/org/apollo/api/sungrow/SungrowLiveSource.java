package org.apollo.api.sungrow;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

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
