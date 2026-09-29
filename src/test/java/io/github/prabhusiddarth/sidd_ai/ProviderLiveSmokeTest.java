package io.github.prabhusiddarth.sidd_ai;

import io.github.prabhusiddarth.sidd_ai.providers.AnthropicChat;
import io.github.prabhusiddarth.sidd_ai.providers.GeminiChat;
import io.github.prabhusiddarth.sidd_ai.providers.GrokChat;
import io.github.prabhusiddarth.sidd_ai.providers.GroqChat;
import io.github.prabhusiddarth.sidd_ai.providers.KimiChat;
import io.github.prabhusiddarth.sidd_ai.providers.NimChat;
import io.github.prabhusiddarth.sidd_ai.providers.OpenAiChat;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Opt-in checks against real provider APIs. Run with:
 * mvn -Dsidd.ai.liveTests=true -Dtest=ProviderLiveSmokeTest test
 */
class ProviderLiveSmokeTest {

    private static final String PROMPT = "Reply with exactly: OK";

    @BeforeAll
    static void requireOptIn() {
        Assumptions.assumeTrue(Boolean.getBoolean("sidd.ai.liveTests"),
                "Live provider tests require -Dsidd.ai.liveTests=true");
    }

    @Test
    void openAi() {
        requireKey("OPENAI_API_KEY");
        assertResponse(new OpenAiChat("gpt-4o-mini").callResponse(PROMPT));
    }

    @Test
    void gemini() {
        requireKey("GEMINI_API_KEY");
        assertResponse(new GeminiChat("gemini-2.5-flash").callResponse(PROMPT));
    }

    @Test
    void anthropic() {
        requireKey("ANTHROPIC_API_KEY");
        assertResponse(new AnthropicChat("claude-haiku-4-5-20251001").callResponse(PROMPT));
    }

    @Test
    void grok() {
        requireKey("GROK_API_KEY");
        assertResponse(new GrokChat("grok-4.3").callResponse(PROMPT));
    }

    @Test
    void groq() {
        requireKey("GROQ_API_KEY");
        assertResponse(new GroqChat("llama-3.1-8b-instant").callResponse(PROMPT));
    }

    @Test
    void nim() {
        requireKey("NIM_API_KEY");
        assertResponse(new NimChat("openai/gpt-oss-20b").callResponse(PROMPT));
    }

    @Test
    void kimi() {
        requireKey("KIMI_API_KEY");
        assertResponse(new KimiChat("kimi-k2.5").callResponse(PROMPT));
    }

    private static void requireKey(String name) {
        String value = EnvHelper.get(name);
        Assumptions.assumeTrue(value != null && !value.isBlank(), name + " is not configured");
    }

    private static void assertResponse(ChatResponse response) {
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isBlank());
    }
}
