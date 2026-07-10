package io.github.prabhusiddarth.sidd_ai;

import org.junit.jupiter.api.Test;

public class DemoTest {

    @Test
    public void runDemo() {
        // Read keys via the new EnvHelper (which checks system env first, then .env)
        String openAiKey = EnvHelper.get("OPENAI_API_KEY");
        String geminiKey = EnvHelper.get("GEMINI_API_KEY");

        if ((openAiKey == null || openAiKey.isEmpty() || openAiKey.contains("your_openai")) &&
                (geminiKey == null || geminiKey.isEmpty() || geminiKey.contains("your_gemini"))) {
            System.out.println("===============================================================");
            System.out.println("Demo skipped: No active OPENAI_API_KEY or GEMINI_API_KEY found.");
            System.out.println("Please set them in your .env file.");
            System.out.println("===============================================================");
            return;
        }

        try {
            if (openAiKey != null && !openAiKey.isEmpty() && !openAiKey.contains("your_openai")) {
                System.out.println("Testing OpenAI connection...");
                String reply = AiClient.chatQuick("gpt-4o", "Say hello in 5 words or less!");
                System.out.println("OpenAI Response: " + reply);
            } else if (geminiKey != null && !geminiKey.isEmpty() && !geminiKey.contains("your_gemini")) {
                System.out.println("Testing Gemini connection...");
                String reply = AiClient.chatQuick("gemini-1.5-flash", "Say hello in 5 words or less!");
                System.out.println("Gemini Response: " + reply);
            }
        } catch (Exception e) {
            System.err.println("API Call failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
