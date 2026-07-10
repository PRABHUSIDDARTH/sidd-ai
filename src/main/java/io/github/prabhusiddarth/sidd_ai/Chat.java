package io.github.prabhusiddarth.sidd_ai;

public interface Chat {
    ChatResponse callResponse(String prompt);

    default String call(String prompt) {
        ChatResponse response = callResponse(prompt);
        return response != null ? response.getContent() : null;
    }

    default ChatResponse callResponse(ChatRequest request) {
        return callResponse(request.getPrompt());
    }

    default String call(ChatRequest request) {
        return callResponse(request).getContent();
    }
}
