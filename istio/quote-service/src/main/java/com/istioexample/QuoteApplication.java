package com.istioexample;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;

public class QuoteApplication {

    @SneakyThrows
    public static void main(String[] args) {
        var server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        var objectMapper = new ObjectMapper();

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        var jokesDbUrl = System.getenv().getOrDefault("JOKES_DB_URL", "http://api.icndb.com");
        var excludeExplicit = System.getenv().getOrDefault("EXCLUDE_EXPLICIT", "true");
        var jokeUrl = jokesDbUrl + "/jokes/random";
        if (Boolean.parseBoolean(excludeExplicit)) {
            jokeUrl += "?exclude=explicit";
        }
        var request = HttpRequest.newBuilder()
                .uri(URI.create(jokeUrl))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        server.createContext("/joke", exchange -> {
            byte[] responseBody = getJokeAsBytes(objectMapper, client, request);
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.getResponseBody().close();
        });
        server.start();
    }

    @SneakyThrows
    private static byte[] getJokeAsBytes(ObjectMapper objectMapper, HttpClient client, HttpRequest request) {
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var quoteResult = objectMapper.readValue(response.body(), QuoteResult.class);
        var joke = quoteResult.getValue();
        joke.setRating("Check Norris jokes are the best jokes!");
        return objectMapper.writeValueAsBytes(joke);
    }
}
