package io.github.prabhusiddarth.sidd_ai.providers;

import io.github.prabhusiddarth.sidd_ai.exceptions.AiAuthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GeminiChatTest {

    @Test
    public void testGeminiChatAuthExceptionOnEmptyKey() {
        assertThrows(AiAuthException.class, () -> {
            new GeminiChat("gemini-1.5-flash", "");
        });
        assertThrows(AiAuthException.class, () -> {
            new GeminiChat("gemini-1.5-flash", null);
        });
    }

    @Test
    public void testGeminiChatConstructorWithApiKey() {
        GeminiChat chat = new GeminiChat("gemini-1.5-flash", "mock-key");
        assertNotNull(chat);
    }
}
