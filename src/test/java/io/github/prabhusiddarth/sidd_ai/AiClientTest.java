package io.github.prabhusiddarth.sidd_ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AiClientTest {

    @Test
    public void testBuilderInitialization() {
        AiClient client = AiClient.builder()
                .defaultModel("gpt-4o")
                .openAiApiKey("sk-mockopenaiapikey1234567890")
                .geminiApiKey("mockgeminiapikey1234567890")
                .anthropicApiKey("sk-ant-mockapikey12345")
                .ollamaHost("http://localhost:11434")
                .build();

        assertNotNull(client);
    }

    @Test
    public void testChatRequestValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            ChatRequest.builder()
                    .prompt("")
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ChatRequest.builder()
                    .prompt(null)
                    .build();
        });

        ChatRequest request = ChatRequest.builder()
                .prompt("Hello")
                .model("gpt-4")
                .temperature(0.7)
                .maxTokens(100)
                .build();

        assertEquals("Hello", request.getPrompt());
        assertEquals("gpt-4", request.getModel());
        assertEquals(0.7, request.getTemperature());
        assertEquals(100, request.getMaxTokens());
    }

    @Test
    public void testDefaultModelExecutionFailureWhenUnset() {
        AiClient client = AiClient.builder().build();
        assertThrows(IllegalStateException.class, () -> {
            client.chat("Hello");
        });
    }
}
