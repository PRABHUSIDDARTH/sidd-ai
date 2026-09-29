package io.github.prabhusiddarth.sidd_ai.providers;

import io.github.prabhusiddarth.sidd_ai.exceptions.AiApiException;
import io.github.prabhusiddarth.sidd_ai.exceptions.AiAuthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GroqChatTest {

    @Test
    public void testGroqChatAuthExceptionOnEmptyKey() {
        assertThrows(AiAuthException.class, () -> {
            new GroqChat("llama-3.3-70b-versatile", "");
        });
        assertThrows(AiAuthException.class, () -> {
            new GroqChat("llama-3.3-70b-versatile", null);
        });
    }

    @Test
    public void testGroqChatConstructorWithApiKey() {
        GroqChat chat = new GroqChat("llama-3.3-70b-versatile", "gsk_mock-key");
        assertNotNull(chat);
    }

    @Test
    public void testValidateGroqParamsRejectsLogprobs() {
        assertThrows(AiApiException.class, () ->
                GroqChat.validateGroqParams(true, null, null, null));
    }

    @Test
    public void testValidateGroqParamsRejectsLogitBias() {
        assertThrows(AiApiException.class, () ->
                GroqChat.validateGroqParams(null, new Object(), null, null));
    }

    @Test
    public void testValidateGroqParamsRejectsTopLogprobs() {
        assertThrows(AiApiException.class, () ->
                GroqChat.validateGroqParams(null, null, 5, null));
    }

    @Test
    public void testValidateGroqParamsRejectsNGreaterThanOne() {
        assertThrows(AiApiException.class, () ->
                GroqChat.validateGroqParams(null, null, null, 3));
    }

    @Test
    public void testValidateGroqParamsAllowsNullOrNEqualsOne() {
        // Should not throw
        assertDoesNotThrow(() ->
                GroqChat.validateGroqParams(null, null, null, null));
        assertDoesNotThrow(() ->
                GroqChat.validateGroqParams(false, null, null, 1));
    }
}
