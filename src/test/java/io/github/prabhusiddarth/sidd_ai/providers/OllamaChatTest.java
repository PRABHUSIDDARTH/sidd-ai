package io.github.prabhusiddarth.sidd_ai.providers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OllamaChatTest {

    @Test
    public void testOllamaChatInitialization() {
        OllamaChat chat = new OllamaChat("llama3");
        assertNotNull(chat);

        OllamaChat customChat = new OllamaChat("llama3", "http://my-ollama-server:11434/");
        assertNotNull(customChat);
    }
}
