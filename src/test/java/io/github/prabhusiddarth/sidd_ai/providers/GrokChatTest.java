package io.github.prabhusiddarth.sidd_ai.providers;

import io.github.prabhusiddarth.sidd_ai.exceptions.AiAuthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GrokChatTest {

    @Test
    public void testGrokChatAuthExceptionOnEmptyKey() {
        assertThrows(AiAuthException.class, () -> {
            new GrokChat("grok-2", "");
        });
        assertThrows(AiAuthException.class, () -> {
            new GrokChat("grok-2", null);
        });
    }

    @Test
    public void testGrokChatConstructorWithApiKey() {
        GrokChat chat = new GrokChat("grok-2", "xai-mock-key");
        assertNotNull(chat);
    }
}
