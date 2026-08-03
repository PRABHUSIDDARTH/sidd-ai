package io.github.prabhusiddarth.sidd_ai;

import io.github.prabhusiddarth.sidd_ai.providers.AnthropicChat;
import io.github.prabhusiddarth.sidd_ai.providers.GeminiChat;
import io.github.prabhusiddarth.sidd_ai.providers.GrokChat;
import io.github.prabhusiddarth.sidd_ai.providers.KimiChat;
import io.github.prabhusiddarth.sidd_ai.providers.NimChat;
import io.github.prabhusiddarth.sidd_ai.providers.OllamaChat;
import io.github.prabhusiddarth.sidd_ai.providers.OpenAiChat;
import io.github.prabhusiddarth.sidd_ai.router.ModelRouter;

public class AiClient {

    private final String defaultModel;
    private final String openAiApiKey;
    private final String geminiApiKey;
    private final String anthropicApiKey;
    private final String ollamaHost;
    private final String grokApiKey;
    private final String nimApiKey;
    private final String kimiApiKey;

    private AiClient(Builder builder) {
        this.defaultModel = builder.defaultModel;
        this.openAiApiKey = builder.openAiApiKey;
        this.geminiApiKey = builder.geminiApiKey;
        this.anthropicApiKey = builder.anthropicApiKey;
        this.ollamaHost = builder.ollamaHost;
        this.grokApiKey = builder.grokApiKey;
        this.nimApiKey = builder.nimApiKey;
        this.kimiApiKey = builder.kimiApiKey;
    }

    /**
     * Call the default model with a text prompt.
     */
    public String chat(String prompt) {
        if (defaultModel == null) {
            throw new IllegalStateException("Default model is not configured. Please use chat(model, prompt) instead.");
        }
        return chat(defaultModel, prompt);
    }

    /**
     * Call any model with a text prompt using this client's credentials.
     */
    public String chat(String model, String prompt) {
        return chatResponse(model, prompt).getContent();
    }

    /**
     * Call any model and get full response metadata using this client's
     * credentials.
     */
    public ChatResponse chatResponse(String model, String prompt) {
        Chat provider = getProvider(model);
        return provider.callResponse(prompt);
    }

    /**
     * Static utility to call any model with environment variable configuration.
     * Extremely simple, Python-like one-liner.
     */
    public static String chatQuick(String model, String prompt) {
        return ModelRouter.route(model).call(prompt);
    }

    /**
     * Instantiates the provider based on the routed model, applying client
     * overrides if present.
     */
    private Chat getProvider(String model) {
        String lowerModel = model.toLowerCase();
        if (lowerModel.startsWith("gpt-") || lowerModel.startsWith("o1-") || lowerModel.startsWith("o3-")) {
            return openAiApiKey != null ? new OpenAiChat(model, openAiApiKey) : new OpenAiChat(model);
        } else if (lowerModel.startsWith("gemini-")) {
            return geminiApiKey != null ? new GeminiChat(model, geminiApiKey) : new GeminiChat(model);
        } else if (lowerModel.startsWith("claude-")) {
            return anthropicApiKey != null ? new AnthropicChat(model, anthropicApiKey) : new AnthropicChat(model);
        } else if (lowerModel.startsWith("grok-")) {
            return grokApiKey != null ? new GrokChat(model, grokApiKey) : new GrokChat(model);
        } else if (lowerModel.startsWith("nvidia/") || lowerModel.startsWith("nim-")) {
            return nimApiKey != null ? new NimChat(model, nimApiKey) : new NimChat(model);
        } else if (lowerModel.startsWith("moonshot-") || lowerModel.startsWith("kimi-")) {
            return kimiApiKey != null ? new KimiChat(model, kimiApiKey) : new KimiChat(model);
        } else {
            return ollamaHost != null ? new OllamaChat(model, ollamaHost) : new OllamaChat(model);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String defaultModel;
        private String openAiApiKey;
        private String geminiApiKey;
        private String anthropicApiKey;
        private String ollamaHost;
        private String grokApiKey;
        private String nimApiKey;
        private String kimiApiKey;

        public Builder defaultModel(String defaultModel) {
            this.defaultModel = defaultModel;
            return this;
        }

        public Builder openAiApiKey(String openAiApiKey) {
            this.openAiApiKey = openAiApiKey;
            return this;
        }

        public Builder geminiApiKey(String geminiApiKey) {
            this.geminiApiKey = geminiApiKey;
            return this;
        }

        public Builder anthropicApiKey(String anthropicApiKey) {
            this.anthropicApiKey = anthropicApiKey;
            return this;
        }

        public Builder ollamaHost(String ollamaHost) {
            this.ollamaHost = ollamaHost;
            return this;
        }

        public Builder grokApiKey(String grokApiKey) {
            this.grokApiKey = grokApiKey;
            return this;
        }

        public Builder nimApiKey(String nimApiKey) {
            this.nimApiKey = nimApiKey;
            return this;
        }

        public Builder kimiApiKey(String kimiApiKey) {
            this.kimiApiKey = kimiApiKey;
            return this;
        }

        public AiClient build() {
            return new AiClient(this);
        }
    }
}
