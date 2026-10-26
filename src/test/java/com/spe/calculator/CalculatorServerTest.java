package com.spe.calculator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Integration tests: real server on a random port, real HTTP calls. */
class CalculatorServerTest {

    private static CalculatorServer server;
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void startServer() throws IOException {
        server = new CalculatorServer(0);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    private HttpResponse<String> call(String method, String pathAndQuery) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + server.getPort() + pathAndQuery))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String pathAndQuery) throws Exception {
        return call("GET", pathAndQuery);
    }

    @Test
    void addReturnsJsonResult() throws Exception {
        HttpResponse<String> r = get("/api/v1/add?a=2&b=3");
        assertEquals(200, r.statusCode());
        assertEquals("{\"operation\":\"add\",\"a\":2,\"b\":3,\"result\":5}", r.body());
        assertTrue(r.headers().firstValue("Content-Type").orElse("").contains("application/json"));
    }

    @Test
    void subtractMultiplyDivideWork() throws Exception {
        assertEquals("{\"operation\":\"subtract\",\"a\":2,\"b\":3,\"result\":-1}", get("/api/v1/subtract?a=2&b=3").body());
        assertEquals("{\"operation\":\"multiply\",\"a\":2,\"b\":3,\"result\":6}", get("/api/v1/multiply?a=2&b=3").body());
        assertEquals("{\"operation\":\"divide\",\"a\":5,\"b\":2,\"result\":2.5}", get("/api/v1/divide?a=5&b=2").body());
    }

    @Test
    void divideByZeroReturns400() throws Exception {
        HttpResponse<String> r = get("/api/v1/divide?a=1&b=0");
        assertEquals(400, r.statusCode());
        assertEquals("{\"error\":\"Division by zero is not allowed\",\"status\":400}", r.body());
    }

    @Test
    void missingParameterReturns400() throws Exception {
        HttpResponse<String> r = get("/api/v1/add?a=1");
        assertEquals(400, r.statusCode());
        assertTrue(r.body().contains("Missing required parameter: b"));
    }

    @Test
    void nonNumericParameterReturns400() throws Exception {
        HttpResponse<String> r = get("/api/v1/add?a=abc&b=1");
        assertEquals(400, r.statusCode());
        assertTrue(r.body().contains("must be a number"));
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        HttpResponse<String> r = get("/actuator/health");
        assertEquals(200, r.statusCode());
        assertEquals("{\"status\":\"UP\"}", r.body());
    }

    @Test
    void unknownPathReturns404() throws Exception {
        assertEquals(404, get("/nope").statusCode());
        assertEquals(404, get("/api/v1/power?a=1&b=2").statusCode());
    }

    @Test
    void nonGetReturns405() throws Exception {
        assertEquals(405, call("POST", "/api/v1/add?a=1&b=2").statusCode());
    }
}
