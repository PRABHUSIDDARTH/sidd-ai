package io.github.prabhusiddarth.sidd_ai.router;

import io.github.prabhusiddarth.sidd_ai.Chat;
import io.github.prabhusiddarth.sidd_ai.providers.AnthropicChat;
import io.github.prabhusiddarth.sidd_ai.providers.GeminiChat;
import io.github.prabhusiddarth.sidd_ai.providers.GrokChat;
import io.github.prabhusiddarth.sidd_ai.providers.KimiChat;
import io.github.prabhusiddarth.sidd_ai.providers.NimChat;
import io.github.prabhusiddarth.sidd_ai.providers.OllamaChat;
import io.github.prabhusiddarth.sidd_ai.providers.OpenAiChat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelRouterTest {

    @Test
    public void testRouteOpenAi() {
        try {
            Chat chat = ModelRouter.route("gpt-4o");
            assertTrue(chat instanceof OpenAiChat);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("OPENAI_API_KEY"));
        }
    }

    @Test
    public void testRouteGemini() {
        try {
            Chat chat = ModelRouter.route("gemini-1.5-flash");
            assertTrue(chat instanceof GeminiChat);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("GEMINI_API_KEY"));
        }
    }

    @Test
    public void testRouteAnthropic() {
        try {
            Chat chat = ModelRouter.route("claude-3-5-sonnet");
            assertTrue(chat instanceof AnthropicChat);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("ANTHROPIC_API_KEY"));
        }
    }

    @Test
    public void testRouteGrok() {
        try {
            Chat chat = ModelRouter.route("grok-2");
            assertTrue(chat instanceof GrokChat);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("GROK_API_KEY"));
        }
    }

    @Test
    public void testRouteNim() {
        try {
            Chat chat = ModelRouter.route("nvidia/llama-3.1-nemotron-70b-instruct");
            assertTrue(chat instanceof NimChat);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("NIM_API_KEY"));
        }
    }

    @Test
    public void testRouteKimi() {
        try {
            Chat chat = ModelRouter.route("moonshot-v1-8k");
            assertTrue(chat instanceof KimiChat);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("KIMI_API_KEY"));
        }
    }

    @Test
    public void testRouteOllama() {
        Chat chat = ModelRouter.route("llama3");
        assertTrue(chat instanceof OllamaChat);
    }

    @Test
    public void testRouteInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            ModelRouter.route("");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ModelRouter.route(null);
        });
    }
}
