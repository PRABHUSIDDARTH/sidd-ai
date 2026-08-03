package io.github.prabhusiddarth.sidd_ai.providers;

import io.github.prabhusiddarth.sidd_ai.exceptions.AiAuthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NimChatTest {

    @Test
    public void testNimChatAuthExceptionOnEmptyKey() {
        assertThrows(AiAuthException.class, () -> {
            new NimChat("nvidia/llama-3.1-nemotron-70b-instruct", "");
        });
        assertThrows(AiAuthException.class, () -> {
            new NimChat("nvidia/llama-3.1-nemotron-70b-instruct", null);
        });
    }

    @Test
    public void testNimChatConstructorWithApiKey() {
        NimChat chat = new NimChat("nvidia/llama-3.1-nemotron-70b-instruct", "nvapi-mock-key");
        assertNotNull(chat);
    }
}
