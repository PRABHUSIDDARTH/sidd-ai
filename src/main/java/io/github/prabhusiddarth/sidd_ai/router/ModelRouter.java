package io.github.prabhusiddarth.sidd_ai.router;

import io.github.prabhusiddarth.sidd_ai.Chat;
import io.github.prabhusiddarth.sidd_ai.providers.AnthropicChat;
import io.github.prabhusiddarth.sidd_ai.providers.GeminiChat;
import io.github.prabhusiddarth.sidd_ai.providers.GrokChat;
import io.github.prabhusiddarth.sidd_ai.providers.GroqChat;
import io.github.prabhusiddarth.sidd_ai.providers.KimiChat;
import io.github.prabhusiddarth.sidd_ai.providers.NimChat;
import io.github.prabhusiddarth.sidd_ai.providers.OllamaChat;
import io.github.prabhusiddarth.sidd_ai.providers.OpenAiChat;

import java.util.Locale;

public final class ModelRouter {

    private ModelRouter() {
    }

    public static Chat route(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model name cannot be empty");
        }

        String lowerModel = model.toLowerCase(Locale.ROOT);

        if (lowerModel.startsWith("gpt-") || lowerModel.startsWith("o1-") || lowerModel.startsWith("o3-")) {
            return new OpenAiChat(model);
        } else if (lowerModel.startsWith("gemini-")) {
            return new GeminiChat(model);
        } else if (lowerModel.startsWith("claude-")) {
            return new AnthropicChat(model);
        } else if (lowerModel.startsWith("grok-")) {
            return new GrokChat(model);
        } else if (lowerModel.startsWith("llama-") || lowerModel.startsWith("groq/")) {
            return new GroqChat(model);
        } else if (lowerModel.startsWith("moonshotai/") || lowerModel.startsWith("moonshot-")
                || lowerModel.startsWith("kimi-")) {
            return new KimiChat(model);
        } else if (isNimModel(lowerModel)) {
            return new NimChat(model);
        } else {
            // Default fallback to Ollama for local models (e.g. llama3, mistral, deepseek)
            return new OllamaChat(model);
        }
    }

    private static boolean isNimModel(String model) {
        return model.startsWith("nvidia/")
                || model.startsWith("nim-")
                || model.startsWith("deepseek-ai/")
                || model.startsWith("z-ai/")
                || model.startsWith("zhipuai/")
                || model.startsWith("qwen/")
                || model.startsWith("minimaxai/")
                || model.startsWith("meta/")
                || model.startsWith("mistralai/")
                || model.startsWith("microsoft/")
                || model.startsWith("ibm/")
                || model.startsWith("openai/");
    }
}
