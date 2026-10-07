package org.apollo.api.recovery;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class BrevoMailClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final String apiUrl;
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    public BrevoMailClient(String apiUrl, String apiKey, String fromEmail, String fromName) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public void send(String to, String subject, String text) {
        if (isBlank(apiKey) || isBlank(fromEmail)) {
            throw new IllegalStateException("Mail is not configured");
        }
        String body = "{\"sender\":{\"name\":" + quote(fromName) + ",\"email\":" + quote(fromEmail) + "},"
                + "\"to\":[{\"email\":" + quote(to) + "}],"
                + "\"subject\":" + quote(subject) + ","
                + "\"textContent\":" + quote(text) + "}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
                .timeout(TIMEOUT)
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Mail API responded " + response.statusCode() + ": "
                        + abbreviate(response.body()));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Mail API unreachable: " + exception.getMessage(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Mail API call interrupted", exception);
        }
    }

    static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : (value == null ? "" : value).toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }

    private static String abbreviate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 200 ? body.substring(0, 200) : body;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
