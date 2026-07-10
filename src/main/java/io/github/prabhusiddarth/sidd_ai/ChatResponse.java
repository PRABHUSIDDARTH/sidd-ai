package io.github.prabhusiddarth.sidd_ai;

public class ChatResponse {
    private final String content;
    private final String model;
    private final int tokensUsed;

    public ChatResponse(String content, String model, int tokensUsed) {
        this.content = content;
        this.model = model;
        this.tokensUsed = tokensUsed;
    }

    public String getContent() {
        return content;
    }

    public String getModel() {
        return model;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "content='" + content + '\'' +
                ", model='" + model + '\'' +
                ", tokensUsed=" + tokensUsed +
                '}';
    }
}
