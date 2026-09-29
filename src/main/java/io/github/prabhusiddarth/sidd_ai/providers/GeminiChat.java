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

public class GeminiChat implements Chat {

    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String model;
    private final String apiKey;

    public GeminiChat(String model) {
        this(model, EnvHelper.get("GEMINI_API_KEY"));
    }

    public GeminiChat(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiAuthException("GEMINI_API_KEY environment variable not set");
        }
    }

    @Override
    public ChatResponse callResponse(String prompt) {
        String url = String.format(ENDPOINT_TEMPLATE, model);
        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiApiException("Failed to call Gemini: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new AiApiException("Failed to call Gemini: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String prompt) {
        try {
            var body = MAPPER.createObjectNode();
            var contents = body.putArray("contents");
            var content = contents.addObject();
            var parts = content.putArray("parts");
            parts.addObject().put("text", prompt);
            return MAPPER.writeValueAsString(body);
        } catch (Exception e) {
            throw new AiApiException("Failed to build request body", e);
        }
    }

    private ChatResponse handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();

        if (status == 401 || status == 403
                || (status == 400 && response.body().contains("API_KEY_INVALID"))) {
            throw new AiAuthException("Invalid Gemini API key");
        }
        if (status == 429) {
            throw new AiRateLimitException("Gemini rate limit exceeded");
        }
        if (status >= 400) {
            throw new AiApiException("Gemini API error (" + status + "): " + response.body());
        }

        try {
            JsonNode root = MAPPER.readTree(response.body());
            String content = root.at("/candidates/0/content/parts/0/text").asText();
            int tokensUsed = root.at("/usageMetadata/totalTokenCount").asInt();
            return new ChatResponse(content, model, tokensUsed);
        } catch (Exception e) {
            throw new AiApiException("Failed to parse Gemini response", e);
        }
    }
}
