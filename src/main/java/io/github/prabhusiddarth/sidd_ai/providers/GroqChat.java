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

/**
 * Groq provider — uses Groq's OpenAI-compatible chat completions endpoint.
 *
 * <p>
 * Base URL: {@code https://api.groq.com/openai/v1}
 *
 * <p>
 * <b>Unsupported fields</b>: Groq does not support {@code logprobs},
 * {@code logit_bias}, {@code top_logprobs}, or {@code messages[].name}.
 * The parameter {@code n} must equal {@code 1} if supplied. Passing any of
 * these via a future extended API will throw an {@link AiApiException}.
 *
 * <p>
 * API key is read from the {@code GROQ_API_KEY} environment variable (or
 * a local {@code .env} file) by default, and may be overridden via the
 * two-argument constructor.
 *
 * <p>
 * Example models: {@code llama-3.3-70b-versatile},
 * {@code openai/gpt-oss-120b}, {@code openai/gpt-oss-20b}.
 */
public class GroqChat implements Chat {

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String model;
    private final String apiKey;

    /**
     * Constructs a {@code GroqChat} instance, reading the API key from the
     * {@code GROQ_API_KEY} environment variable or {@code .env} file.
     *
     * @param model Groq model name, e.g. {@code "llama-3.3-70b-versatile"}
     */
    public GroqChat(String model) {
        this(model, EnvHelper.get("GROQ_API_KEY"));
    }

    /**
     * Constructs a {@code GroqChat} instance with an explicit API key.
     *
     * @param model  Groq model name
     * @param apiKey Groq API key (must not be blank)
     */
    public GroqChat(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiAuthException("GROQ_API_KEY environment variable not set");
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
            throw new AiApiException("Failed to call Groq: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new AiApiException("Failed to call Groq: " + e.getMessage(), e);
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
            // n defaults to 1; Groq requires n == 1
            body.put("n", 1);
            return MAPPER.writeValueAsString(body);
        } catch (Exception e) {
            throw new AiApiException("Failed to build Groq request body", e);
        }
    }

    private ChatResponse handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();

        if (status == 401) {
            throw new AiAuthException("Invalid Groq API key");
        }
        if (status == 429) {
            throw new AiRateLimitException("Groq rate limit exceeded");
        }
        if (status >= 400) {
            throw new AiApiException("Groq API error (" + status + "): " + response.body());
        }

        try {
            JsonNode root = MAPPER.readTree(response.body());
            String content = root.at("/choices/0/message/content").asText();
            int tokensUsed = root.at("/usage/total_tokens").asInt();
            return new ChatResponse(content, model, tokensUsed);
        } catch (Exception e) {
            throw new AiApiException("Failed to parse Groq response", e);
        }
    }

    /**
     * Validates that the caller has not supplied fields that Groq does not
     * support. Call this helper if you extend this class to accept raw request
     * parameters.
     *
     * <p>
     * Unsupported fields: {@code logprobs}, {@code logit_bias},
     * {@code top_logprobs}, {@code messages[].name}; {@code n} must be 1.
     *
     * @param n value of the {@code n} parameter, or {@code null} if not set
     * @throws AiApiException if any unsupported field is detected
     */
    public static void validateGroqParams(Boolean logprobs, Object logitBias,
            Integer topLogprobs, Integer n) {
        if (Boolean.TRUE.equals(logprobs)) {
            throw new AiApiException(
                    "Groq does not support the 'logprobs' parameter");
        }
        if (logitBias != null) {
            throw new AiApiException(
                    "Groq does not support the 'logit_bias' parameter");
        }
        if (topLogprobs != null) {
            throw new AiApiException(
                    "Groq does not support the 'top_logprobs' parameter");
        }
        if (n != null && n != 1) {
            throw new AiApiException(
                    "Groq requires 'n' to be 1 (received " + n + ")");
        }
    }
}
