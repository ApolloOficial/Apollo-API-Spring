package org.apollo.api.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BrevoMailClientTest {

    private HttpServer server;
    private final AtomicReference<String> body = new AtomicReference<>();
    private final AtomicReference<String> apiKey = new AtomicReference<>();
    private volatile int status = 201;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/send", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            apiKey.set(exchange.getRequestHeaders().getFirst("api-key"));
            byte[] response = "{\"message\":\"x\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private BrevoMailClient client(String key, String from) {
        return new BrevoMailClient("http://127.0.0.1:" + server.getAddress().getPort() + "/send", key, from, "Apollo");
    }

    @Test
    void shouldPostTheMessageWithTheApiKey() {
        client("secret", "apollo@x.com").send("enzo@x.com", "Assunto", "Linha 1\nCódigo \"123456\"");

        assertEquals("secret", apiKey.get());
        assertTrue(body.get().contains("\"email\":\"apollo@x.com\""));
        assertTrue(body.get().contains("\"to\":[{\"email\":\"enzo@x.com\"}]"));
        assertTrue(body.get().contains("Linha 1\\nCódigo \\\"123456\\\""));
    }

    @Test
    void shouldFailWhenTheApiRejectsTheRequest() {
        status = 401;

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> client("bad", "apollo@x.com").send("enzo@x.com", "A", "B"));

        assertTrue(error.getMessage().contains("401"));
    }

    @Test
    void shouldFailWhenNotConfigured() {
        assertThrows(IllegalStateException.class, () -> client("", "apollo@x.com").send("a@x.com", "A", "B"));
        assertThrows(IllegalStateException.class, () -> client("key", " ").send("a@x.com", "A", "B"));
    }

    @Test
    void shouldEscapeControlCharacters() {
        assertEquals("\"a\\\\b\\t\\u0001\"", BrevoMailClient.quote("a\\b\t\u0001"));
    }
}
