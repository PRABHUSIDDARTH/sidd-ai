package io.github.prabhusiddarth.sidd_ai.router;

import io.github.prabhusiddarth.sidd_ai.Chat;
import io.github.prabhusiddarth.sidd_ai.providers.AnthropicChat;
import io.github.prabhusiddarth.sidd_ai.providers.GeminiChat;
import io.github.prabhusiddarth.sidd_ai.providers.OllamaChat;
import io.github.prabhusiddarth.sidd_ai.providers.OpenAiChat;

public class ModelRouter {

    public static Chat route(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model name cannot be empty");
        }

        String lowerModel = model.toLowerCase();

        if (lowerModel.startsWith("gpt-") || lowerModel.startsWith("o1-") || lowerModel.startsWith("o3-")) {
            return new OpenAiChat(model);
        } else if (lowerModel.startsWith("gemini-")) {
            return new GeminiChat(model);
        } else if (lowerModel.startsWith("claude-")) {
            return new AnthropicChat(model);
        } else {
            // Default fallback to Ollama for local models (e.g. llama3, mistral, deepseek)
            return new OllamaChat(model);
        }
    }
}
