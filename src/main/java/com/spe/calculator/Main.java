package com.spe.calculator;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        String env = System.getenv("PORT");
        if (env != null && !env.isBlank()) {
            port = Integer.parseInt(env.trim());
        }

        CalculatorServer server = new CalculatorServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
        System.out.println("Calculator API listening on port " + server.getPort());
    }
}
