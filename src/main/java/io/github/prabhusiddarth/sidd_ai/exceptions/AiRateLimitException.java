package io.github.prabhusiddarth.sidd_ai.exceptions;

public class AiRateLimitException extends AiException {
    public AiRateLimitException(String message) {
        super(message);
    }

    public AiRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
