package io.github.prabhusiddarth.sidd_ai;

public class ChatRequest {
    private final String prompt;
    private final String model;
    private final Double temperature;
    private final Integer maxTokens;

    private ChatRequest(Builder builder) {
        this.prompt = builder.prompt;
        this.model = builder.model;
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getModel() {
        return model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String prompt;
        private String model;
        private Double temperature;
        private Integer maxTokens;

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public ChatRequest build() {
            if (prompt == null || prompt.isBlank()) {
                throw new IllegalArgumentException("Prompt cannot be empty");
            }
            return new ChatRequest(this);
        }
    }
}
