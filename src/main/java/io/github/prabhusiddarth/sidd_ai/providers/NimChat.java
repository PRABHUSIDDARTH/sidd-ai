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

public class NimChat implements Chat {

    private static final String ENDPOINT = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String model;
    private final String apiKey;

    public NimChat(String model) {
        this(model, EnvHelper.get("NIM_API_KEY"));
    }

    public NimChat(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiAuthException("NIM_API_KEY environment variable not set");
        }
    }

    @Override
    public ChatResponse callResponse(String prompt) {
        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiApiException("Failed to call NIM: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new AiApiException("Failed to call NIM: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String prompt) {
        try {
            var body = MAPPER.createObjectNode();
            body.put("model", model);
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
            throw new AiAuthException("Invalid NIM API key");
        }
        if (status == 429) {
            throw new AiRateLimitException("NIM rate limit exceeded");
        }
        if (status >= 400) {
            throw new AiApiException("NIM API error (" + status + "): " + response.body());
        }

        try {
            JsonNode root = MAPPER.readTree(response.body());
            String content = root.at("/choices/0/message/content").asText();
            int tokensUsed = root.at("/usage/total_tokens").asInt();
            return new ChatResponse(content, model, tokensUsed);
        } catch (Exception e) {
            throw new AiApiException("Failed to parse NIM response", e);
        }
    }
}
