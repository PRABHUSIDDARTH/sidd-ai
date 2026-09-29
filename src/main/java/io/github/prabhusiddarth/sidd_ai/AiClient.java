package io.github.prabhusiddarth.sidd_ai;

import io.github.prabhusiddarth.sidd_ai.providers.AnthropicChat;
import io.github.prabhusiddarth.sidd_ai.providers.GeminiChat;
import io.github.prabhusiddarth.sidd_ai.providers.GrokChat;
import io.github.prabhusiddarth.sidd_ai.providers.GroqChat;
import io.github.prabhusiddarth.sidd_ai.providers.KimiChat;
import io.github.prabhusiddarth.sidd_ai.providers.NimChat;
import io.github.prabhusiddarth.sidd_ai.providers.OllamaChat;
import io.github.prabhusiddarth.sidd_ai.providers.OpenAiChat;
import io.github.prabhusiddarth.sidd_ai.router.ModelRouter;

import java.util.Locale;

public final class AiClient {

    private final String defaultModel;
    private final String openAiApiKey;
    private final String geminiApiKey;
    private final String anthropicApiKey;
    private final String ollamaHost;
    private final String grokApiKey;
    private final String groqApiKey;
    private final String nimApiKey;
    private final String kimiApiKey;

    private AiClient(Builder builder) {
        this.defaultModel = builder.defaultModel;
        this.openAiApiKey = builder.openAiApiKey;
        this.geminiApiKey = builder.geminiApiKey;
        this.anthropicApiKey = builder.anthropicApiKey;
        this.ollamaHost = builder.ollamaHost;
        this.grokApiKey = builder.grokApiKey;
        this.groqApiKey = builder.groqApiKey;
        this.nimApiKey = builder.nimApiKey;
        this.kimiApiKey = builder.kimiApiKey;
    }

    public String chat(String prompt) {
        if (defaultModel == null) {
            throw new IllegalStateException("Default model is not configured. Please use chat(model, prompt) instead.");
        }
        return chat(defaultModel, prompt);
    }

    /**
     * Infers provider from model string prefix (kept for backward compat / quick
     * calls).
     */
    public String chat(String model, String prompt) {
        return chatResponse(model, prompt).getContent();
    }

    public ChatResponse chatResponse(String model, String prompt) {
        Chat provider = getProvider(model);
        return provider.callResponse(prompt);
    }

    /**
     * Explicit provider selection — use when the caller already knows which
     * provider was picked (e.g. a menu-driven CLI), instead of guessing from the
     * model string.
     * providerKey: "gemini" | "openai" | "anthropic" | "grok" | "groq" | "nim" | "kimi" |
     * "ollama"
     */
    public String chatWithProvider(String providerKey, String model, String prompt) {
        return chatResponseWithProvider(providerKey, model, prompt).getContent();
    }

    public ChatResponse chatResponseWithProvider(String providerKey, String model, String prompt) {
        Chat provider = getProviderByKey(providerKey, model);
        return provider.callResponse(prompt);
    }

    public ChatResponse chatResponse(String providerKey, String model, String prompt) {
        Chat provider = getProviderByKey(providerKey, model);
        return provider.callResponse(prompt);
    }

    public static String chatQuick(String model, String prompt) {
        return ModelRouter.route(model).call(prompt);
    }

    private Chat getProviderByKey(String providerKey, String model) {
        if (providerKey == null || providerKey.isBlank()) {
            throw new IllegalArgumentException("Provider key cannot be empty");
        }
        return switch (providerKey.toLowerCase(Locale.ROOT)) {
            case "gemini" -> geminiApiKey != null ? new GeminiChat(model, geminiApiKey) : new GeminiChat(model);
            case "openai" -> openAiApiKey != null ? new OpenAiChat(model, openAiApiKey) : new OpenAiChat(model);
            case "anthropic", "claude" ->
                anthropicApiKey != null ? new AnthropicChat(model, anthropicApiKey) : new AnthropicChat(model);
            case "grok" -> grokApiKey != null ? new GrokChat(model, grokApiKey) : new GrokChat(model);
            case "groq" -> groqApiKey != null ? new GroqChat(model, groqApiKey) : new GroqChat(model);
            case "nim", "nvidia" -> nimApiKey != null ? new NimChat(model, nimApiKey) : new NimChat(model);
            case "kimi", "moonshot" -> kimiApiKey != null ? new KimiChat(model, kimiApiKey) : new KimiChat(model);
            case "ollama" -> ollamaHost != null ? new OllamaChat(model, ollamaHost) : new OllamaChat(model);
            default -> throw new IllegalArgumentException("Unknown provider key: " + providerKey);
        };
    }

    /**
     * Fallback: infers provider from model string prefix. Used by chat(model,
     * prompt) and chatQuick().
     */
    private Chat getProvider(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model name cannot be empty");
        }
        String lowerModel = model.toLowerCase(Locale.ROOT);
        if (lowerModel.startsWith("gpt-") || lowerModel.startsWith("o1-") || lowerModel.startsWith("o3-")) {
            return openAiApiKey != null ? new OpenAiChat(model, openAiApiKey) : new OpenAiChat(model);
        } else if (lowerModel.startsWith("gemini-")) {
            return geminiApiKey != null ? new GeminiChat(model, geminiApiKey) : new GeminiChat(model);
        } else if (lowerModel.startsWith("claude-")) {
            return anthropicApiKey != null ? new AnthropicChat(model, anthropicApiKey) : new AnthropicChat(model);
        } else if (lowerModel.startsWith("grok-")) {
            return grokApiKey != null ? new GrokChat(model, grokApiKey) : new GrokChat(model);
        } else if (lowerModel.startsWith("llama-") || lowerModel.startsWith("groq/")) {
            return groqApiKey != null ? new GroqChat(model, groqApiKey) : new GroqChat(model);
        } else if (lowerModel.startsWith("moonshotai/") || lowerModel.startsWith("moonshot-")
                || lowerModel.startsWith("kimi-")) {
            return kimiApiKey != null ? new KimiChat(model, kimiApiKey) : new KimiChat(model);
        } else if (lowerModel.startsWith("nvidia/")
                || lowerModel.startsWith("nim-")
                || lowerModel.startsWith("deepseek-ai/")
                || lowerModel.startsWith("z-ai/")
                || lowerModel.startsWith("zhipuai/")
                || lowerModel.startsWith("qwen/")
                || lowerModel.startsWith("minimaxai/")
                || lowerModel.startsWith("meta/")
                || lowerModel.startsWith("mistralai/")
                || lowerModel.startsWith("microsoft/")
                || lowerModel.startsWith("ibm/")
                || lowerModel.startsWith("openai/")) {
            return nimApiKey != null ? new NimChat(model, nimApiKey) : new NimChat(model);
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
        private String groqApiKey;
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

        public Builder groqApiKey(String groqApiKey) {
            this.groqApiKey = groqApiKey;
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
