package org.apollo.api.sungrow;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Liga as propriedades da Sungrow e, so quando apollo.sungrow.enabled=true, cria o RestClient.
 * Mesmo molde do ChatbotConfig (JDK HttpClient, timeout de conexao de 10 s).
 */
@Configuration
@EnableConfigurationProperties(SungrowProperties.class)
public class SungrowConfig {

    @Bean
    @ConditionalOnSungrowLive
    RestClient sungrowRestClient(SungrowProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
