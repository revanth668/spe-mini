package com.spe.calculator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** HTTP layer built on the JDK's built-in HttpServer (no framework). */
public class CalculatorServer {

    private static final String API_PREFIX = "/api/v1/";

    private final Calculator calculator = new Calculator();
    private final HttpServer server;

    public CalculatorServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/", this::route);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    /** Actual bound port (useful when started with port 0 in tests). */
    public int getPort() {
        return server.getAddress().getPort();
    }

    private void route(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            boolean known = path.equals("/actuator/health")
                    || (path.startsWith(API_PREFIX) && isOperation(path.substring(API_PREFIX.length())));

            if (!known) {
                send(ex, 404, JsonUtil.error("Not found: " + path, 404));
                return;
            }
            if (!"GET".equals(ex.getRequestMethod())) {
                send(ex, 405, JsonUtil.error("Method not allowed: " + ex.getRequestMethod(), 405));
                return;
            }
            if (path.equals("/actuator/health")) {
                send(ex, 200, "{\"status\":\"UP\"}");
                return;
            }
            handleOperation(ex, path.substring(API_PREFIX.length()));
        } catch (Exception e) {
            send(ex, 500, JsonUtil.error("Internal server error", 500));
        }
    }

    private static boolean isOperation(String op) {
        return op.equals("add") || op.equals("subtract") || op.equals("multiply") || op.equals("divide");
    }

    private void handleOperation(HttpExchange ex, String op) throws IOException {
        Map<String, String> params = parseQuery(ex.getRequestURI().getRawQuery());
        double a;
        double b;
        try {
            a = parseNumber(params, "a");
            b = parseNumber(params, "b");
        } catch (IllegalArgumentException e) {
            send(ex, 400, JsonUtil.error(e.getMessage(), 400));
            return;
        }

        double result;
        try {
            result = switch (op) {
                case "add" -> calculator.add(a, b);
                case "subtract" -> calculator.subtract(a, b);
                case "multiply" -> calculator.multiply(a, b);
                default -> calculator.divide(a, b);
            };
        } catch (ArithmeticException e) {
            send(ex, 400, JsonUtil.error(e.getMessage(), 400));
            return;
        }
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            send(ex, 400, JsonUtil.error("Result is out of range", 400));
            return;
        }
        send(ex, 200, JsonUtil.result(op, a, b, result));
    }

    private static double parseNumber(Map<String, String> params, String name) {
        String raw = params.get(name);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Missing required parameter: " + name);
        }
        double value;
        try {
            value = Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter '" + name + "' must be a number");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Parameter '" + name + "' must be a finite number");
        }
        return value;
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return map;
        }
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            String key = idx >= 0 ? pair.substring(0, idx) : pair;
            String value = idx >= 0 ? pair.substring(idx + 1) : "";
            map.put(URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return map;
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
