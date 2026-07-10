package io.github.prabhusiddarth.sidd_ai.providers;

import io.github.prabhusiddarth.sidd_ai.exceptions.AiAuthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnthropicChatTest {

    @Test
    public void testAnthropicChatAuthExceptionOnEmptyKey() {
        assertThrows(AiAuthException.class, () -> {
            new AnthropicChat("claude-3-5-sonnet", "");
        });
        assertThrows(AiAuthException.class, () -> {
            new AnthropicChat("claude-3-5-sonnet", null);
        });
    }

    @Test
    public void testAnthropicChatConstructorWithApiKey() {
        AnthropicChat chat = new AnthropicChat("claude-3-5-sonnet", "sk-ant-mock-key");
        assertNotNull(chat);
    }
}
