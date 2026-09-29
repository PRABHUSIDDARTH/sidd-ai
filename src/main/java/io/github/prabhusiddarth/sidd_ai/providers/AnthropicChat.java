package io.github.prabhusiddarth.sidd_ai.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.prabhusiddarth.sidd_ai.Chat;
import io.github.prabhusiddarth.sidd_ai.ChatResponse;
import io.github.prabhusiddarth.sidd_ai.EnvHelper;
import io.github.prabhusiddarth.sidd_ai.exceptions.AiApiException;
import io.github.prabhusiddarth.sidd_ai.exceptions.AiAuthException;
import io.github.prabhusiddarth.sidd_ai.exceptions.AiRateLimitException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AnthropicChat implements Chat {

    private static final String ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String model;
    private final String apiKey;

    public AnthropicChat(String model) {
        this(model, EnvHelper.get("ANTHROPIC_API_KEY"));
    }

    public AnthropicChat(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiAuthException("ANTHROPIC_API_KEY environment variable not set");
        }
    }

    @Override
    public ChatResponse callResponse(String prompt) {
        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiApiException("Failed to call Anthropic: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new AiApiException("Failed to call Anthropic: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String prompt) {
        try {
            var body = MAPPER.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", 1024);
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

        if (status == 401) {
            throw new AiAuthException("Invalid Anthropic API key");
        }
        if (status == 429) {
            throw new AiRateLimitException("Anthropic rate limit exceeded");
        }
        if (status >= 400) {
            throw new AiApiException("Anthropic API error (" + status + "): " + response.body());
        }

        try {
            JsonNode root = MAPPER.readTree(response.body());
            String content = root.at("/content/0/text").asText();
            int inputTokens = root.at("/usage/input_tokens").asInt();
            int outputTokens = root.at("/usage/output_tokens").asInt();
            return new ChatResponse(content, model, inputTokens + outputTokens);
        } catch (Exception e) {
            throw new AiApiException("Failed to parse Anthropic response", e);
        }
    }
}
