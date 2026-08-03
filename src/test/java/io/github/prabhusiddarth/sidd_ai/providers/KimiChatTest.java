package io.github.prabhusiddarth.sidd_ai.providers;

import io.github.prabhusiddarth.sidd_ai.exceptions.AiAuthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KimiChatTest {

    @Test
    public void testKimiChatAuthExceptionOnEmptyKey() {
        assertThrows(AiAuthException.class, () -> {
            new KimiChat("moonshot-v1-8k", "");
        });
        assertThrows(AiAuthException.class, () -> {
            new KimiChat("moonshot-v1-8k", null);
        });
    }

    @Test
    public void testKimiChatConstructorWithApiKey() {
        KimiChat chat = new KimiChat("moonshot-v1-8k", "sk-moon-mock-key");
        assertNotNull(chat);
    }
}
