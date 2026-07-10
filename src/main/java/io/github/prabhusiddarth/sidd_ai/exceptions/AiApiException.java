package io.github.prabhusiddarth.sidd_ai.exceptions;

public class AiApiException extends AiException {
    public AiApiException(String message) {
        super(message);
    }

    public AiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
