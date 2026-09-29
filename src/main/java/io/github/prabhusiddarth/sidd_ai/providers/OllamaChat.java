package io.github.prabhusiddarth.sidd_ai.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.prabhusiddarth.sidd_ai.Chat;
import io.github.prabhusiddarth.sidd_ai.ChatResponse;
import io.github.prabhusiddarth.sidd_ai.EnvHelper;
import io.github.prabhusiddarth.sidd_ai.exceptions.AiApiException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OllamaChat implements Chat {

    private static final String DEFAULT_HOST = "http://localhost:11434";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String model;
    private final String endpoint;

    public OllamaChat(String model) {
        this(model, getOllamaHost());
    }

    public OllamaChat(String model, String host) {
        this.model = model;
        if (host == null || host.isBlank()) {
            host = DEFAULT_HOST;
        }
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        this.endpoint = host + "/api/chat";
    }

    private static String getOllamaHost() {
        String host = EnvHelper.get("OLLAMA_HOST");
        return (host == null || host.isBlank()) ? DEFAULT_HOST : host;
    }

    @Override
    public ChatResponse callResponse(String prompt) {
        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiApiException("Failed to call Ollama: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new AiApiException("Failed to call Ollama: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String prompt) {
        try {
            var body = MAPPER.createObjectNode();
            body.put("model", model);
            body.put("stream", false);
            var messages = body.putArray("messages");
            var message = messages.addObject();
            message.put("role", "user");
            message.put("content", prompt);
            return MAPPER.writeValueAsString(body);
        } catch (Exception e) {
            throw new AiApiException("Failed to build request body", e);
        }
    }

    private ChatResponse handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();

        if (status >= 400) {
            throw new AiApiException("Ollama API error (" + status + "): " + response.body());
        }

        try {
            JsonNode root = MAPPER.readTree(response.body());
            String content = root.at("/message/content").asText();
            int promptEvalCount = root.at("/prompt_eval_count").asInt();
            int evalCount = root.at("/eval_count").asInt();
            return new ChatResponse(content, model, promptEvalCount + evalCount);
        } catch (Exception e) {
            throw new AiApiException("Failed to parse Ollama response", e);
        }
    }
}
