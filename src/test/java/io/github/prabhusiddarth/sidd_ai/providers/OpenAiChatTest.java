package io.github.prabhusiddarth.sidd_ai.providers;

import io.github.prabhusiddarth.sidd_ai.exceptions.AiAuthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenAiChatTest {

    @Test
    public void testOpenAiChatAuthExceptionOnEmptyKey() {
        assertThrows(AiAuthException.class, () -> {
            new OpenAiChat("gpt-4o", "");
        });
        assertThrows(AiAuthException.class, () -> {
            new OpenAiChat("gpt-4o", null);
        });
    }

    @Test
    public void testOpenAiChatConstructorWithApiKey() {
        OpenAiChat chat = new OpenAiChat("gpt-4o", "sk-mock-key");
        assertNotNull(chat);
    }
}
