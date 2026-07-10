# sidd-ai — Line-by-Line Learning Guide

> Complete explanation of every class, every method, and every line of code in the sidd-ai package.

---

## Table of Contents
1. [How to Read This Guide](#1-how-to-read-this-guide)
2. [pom.xml — Build Configuration](#2-pomxml--build-configuration)
3. [Exception Hierarchy](#3-exception-hierarchy)
4. [Chat.java — The Core Interface](#4-chatjava--the-core-interface)
5. [ChatResponse.java — The Response Model](#5-chatresponsejava--the-response-model)
6. [ChatRequest.java — The Request Builder](#6-chatrequestjava--the-request-builder)
7. [OpenAiChat.java — OpenAI Provider](#7-openaichatjava--openai-provider)
8. [GeminiChat.java — Google Gemini Provider](#8-geminichatjava--google-gemini-provider)
9. [AnthropicChat.java — Anthropic Claude Provider](#9-anthropicchatjava--anthropic-claude-provider)
10. [OllamaChat.java — Local Ollama Provider](#10-ollamachatjava--local-ollama-provider)
11. [ModelRouter.java — The Request Router](#11-modelrouterjava--the-request-router)
12. [AiClient.java — The Main Entry Point](#12-aiclientjava--the-main-entry-point)
13. [Usage Examples](#13-usage-examples)

---

## 1. How to Read This Guide

Each section covers one file. For every block of code:
- ✅ We explain **what the code is doing**
- 🧠 We explain **why it was written that way**
- 💡 We highlight key Java concepts used

---

## 2. pom.xml — Build Configuration

The `pom.xml` is the Maven project descriptor. It tells Maven how to compile the project, which external libraries to download, and how to run tests.

```xml
<groupId>io.github.prabhusiddarth</groupId>
<artifactId>sidd-ai</artifactId>
<version>1.0.0</version>
```
- **groupId**: Your organization identifier (reverse domain: `io.github.prabhusiddarth`). This is the namespace for publishing to Maven Central.
- **artifactId**: The library's unique name: `sidd-ai`.
- **version**: Follows Semantic Versioning — `1.0.0` is the first stable release.

```xml
<properties>
    <maven.compiler.release>17</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```
- **`maven.compiler.release`**: Tells the Java compiler to target Java 17. Using `release` (not `source`/`target`) also sets the system modules path automatically — fixing IDE warnings.

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```
- **Jackson Databind**: The #1 JSON library in Java. We use it to:
  - Build JSON bodies to send to AI APIs (`ObjectMapper.createObjectNode()`)
  - Parse JSON responses back (`ObjectMapper.readTree()`)

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>
```
- **JUnit 5**: The modern Java testing framework. `scope=test` means it is only compiled into test classes, not the production JAR.

```xml
<build>
    <sourceDirectory>src/main/java</sourceDirectory>
    <testSourceDirectory>src/test/java</testSourceDirectory>
</build>
```
- **Explicit source roots**: Tells Maven AND the IDE (RedHat Java Language Server) exactly where source files live, preventing the "declared package does not match expected package" error in IDE editors.

---

## 3. Exception Hierarchy

We created a custom exception hierarchy so callers can catch specific error types precisely.

### Why not use plain `Exception`?

If all errors threw a generic `RuntimeException`, the user couldn't write code like:
```java
try {
    String reply = client.chat("gpt-4o", "Hello");
} catch (AiAuthException e) {
    // Show the user a "please check your API key" message
} catch (AiRateLimitException e) {
    // Retry after a delay
}
```

### `AiException.java` — Root Exception

```java
package io.github.prabhusiddarth.sidd_ai.exceptions;

public class AiException extends RuntimeException {
```
- **Extends `RuntimeException`**: This makes it an *unchecked* exception. The caller is NOT forced to write a `try-catch` block — they can let it propagate up. This matches the style of modern Java SDKs (like the AWS SDK).

```java
    public AiException(String message) {
        super(message);        // Pass the message to RuntimeException's storage
    }

    public AiException(String message, Throwable cause) {
        super(message, cause); // Wrap an underlying cause (e.g. IOException)
    }
```
- Always provide a `cause` constructor so that the original stack trace is preserved when wrapping lower-level exceptions.

### `AiAuthException.java`
Thrown on HTTP `401` (Unauthorized) or `403` (Forbidden) responses.
```java
public class AiAuthException extends AiException { ... }
```

### `AiRateLimitException.java`
Thrown on HTTP `429` (Too Many Requests).
```java
public class AiRateLimitException extends AiException { ... }
```

### `AiApiException.java`
Thrown on any other HTTP error (`>= 400`) or network failure.
```java
public class AiApiException extends AiException { ... }
```

---

## 4. Chat.java — The Core Interface

```java
package io.github.prabhusiddarth.sidd_ai;

public interface Chat {
```
- **`interface`**: Defines a contract. Any class that says `implements Chat` MUST provide the method `callResponse(String prompt)`. This is the **Strategy Pattern** — the interface is the strategy.

```java
    ChatResponse callResponse(String prompt);
```
- This is the **only method** every provider MUST implement.
- It takes a text `prompt` as input and returns a `ChatResponse` containing the AI's reply.

```java
    default String call(String prompt) {
        ChatResponse response = callResponse(prompt);
        return response != null ? response.getContent() : null;
    }
```
- **`default` method**: Introduced in Java 8. Provides an implementation directly inside the interface. Subclasses get this for free — they do NOT need to override it.
- **Purpose**: Returns just the text content string (not the full `ChatResponse`), making a simple one-liner possible: `String reply = geminiChat.call("Hello");`

```java
    default ChatResponse callResponse(ChatRequest request) {
        return callResponse(request.getPrompt());
    }

    default String call(ChatRequest request) {
        return callResponse(request).getContent();
    }
```
- Convenience overloads that accept a structured `ChatRequest` instead of a plain `String`.
- They delegate to `callResponse(String)` — providers don't need extra code for `ChatRequest` support.

---

## 5. ChatResponse.java — The Response Model

```java
public class ChatResponse {
    private final String content;
    private final String model;
    private final int tokensUsed;
```
- **`final` fields**: These can only be assigned once (in the constructor). This makes `ChatResponse` *immutable* — once created, no data can change. Immutable objects are safe to pass between threads.
- **`content`**: The AI's reply text.
- **`model`**: The model that generated the response (e.g. `"gemini-1.5-flash"`).
- **`tokensUsed`**: Total token count from the response — useful for cost monitoring.

```java
    public ChatResponse(String content, String model, int tokensUsed) {
        this.content = content;
        this.model = model;
        this.tokensUsed = tokensUsed;
    }
```
- Standard all-args constructor. Since fields are `final`, they can only be set here.

```java
    @Override
    public String toString() {
        return "ChatResponse{content='" + content + "', model='" + model + "', tokensUsed=" + tokensUsed + '}';
    }
```
- Overrides Java's default `Object.toString()` so that `System.out.println(response)` shows readable content instead of a memory address like `ChatResponse@5a07e868`.

---

## 6. ChatRequest.java — The Request Builder

This uses the **Builder Pattern** to make optional configuration clean and readable.

```java
public class ChatRequest {
    private final String prompt;
    private final String model;
    private final Double temperature;
    private final Integer maxTokens;

    private ChatRequest(Builder builder) {  // ← Private constructor!
        this.prompt = builder.prompt;
        this.model = builder.model;
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
    }
```
- The constructor is **private** — you CANNOT do `new ChatRequest(...)`. The only way to create one is through the `Builder`.
- 🧠 **Why?** With a 4-argument constructor, you might confuse which argument is which. `new ChatRequest("Hello", "gpt-4", 0.7, 100)` vs `new ChatRequest("gpt-4", "Hello", 100, 0.7)` — which is right? The builder makes it explicit.

```java
    public static Builder builder() {
        return new Builder();
    }
```
- A static factory method that creates a new `Builder` instance. This is the entry point.

```java
    public static class Builder {
        private String prompt;
        private String model;
        private Double temperature;
        private Integer maxTokens;

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;  // ← Returns 'this' for method chaining
        }
```
- Each setter returns `this` (the Builder object) so that calls can be chained: `.prompt(...).model(...).temperature(...)`.

```java
        public ChatRequest build() {
            if (prompt == null || prompt.isBlank()) {
                throw new IllegalArgumentException("Prompt cannot be empty");
            }
            return new ChatRequest(this);
        }
```
- **Validation at build time**: `prompt` is mandatory. If it's missing or empty, we throw an `IllegalArgumentException` *before* creating the object — you can never have an invalid `ChatRequest` in memory.

---

## 7. OpenAiChat.java — OpenAI Provider

```java
private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";
```
- The endpoint for OpenAI's Chat Completions API.

```java
private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
```
- **`static final`**: One shared HTTP client for the whole class. Creating `HttpClient` is expensive — it manages a thread pool and connection pool. Sharing one instance is efficient.
- **`connectTimeout(10s)`**: If the server doesn't respond to the initial TCP handshake within 10 seconds, it throws an exception instead of hanging forever.

```java
private static final ObjectMapper MAPPER = new ObjectMapper();
```
- Jackson's JSON parser/generator. Also `static` because creating it is expensive and it's thread-safe.

```java
public OpenAiChat(String model) {
    this(model, System.getenv("OPENAI_API_KEY")); // Reads env variable
}

public OpenAiChat(String model, String apiKey) {
    this.model = model;
    this.apiKey = apiKey;
    if (apiKey == null || apiKey.isBlank()) {
        throw new AiAuthException("OPENAI_API_KEY environment variable not set");
    }
}
```
- **Two constructors**: The first (one arg) reads the key from the OS environment — great for production. The second (two args) accepts a programmatic key — great for testing and configuration.
- **`this(...)`**: The first constructor delegates to the second — avoids duplicate code. Called *constructor chaining*.

```java
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(ENDPOINT))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + apiKey)
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();
```
- Builds an HTTP POST request using Java 11+'s built-in `java.net.http` package (no extra library needed).
- `Authorization: Bearer KEY` is OpenAI's required auth header.

```java
private String buildRequestBody(String prompt) {
    try {
        var body = MAPPER.createObjectNode();     // Create {}
        body.put("model", model);                // {"model": "gpt-4o"}
        var messages = body.putArray("messages"); // {"messages": []}
        var message = messages.addObject();       // {"messages": [{}]}
        message.put("role", "user");             // {"role": "user"}
        message.put("content", prompt);          // {"content": "Hello"}
        return MAPPER.writeValueAsString(body);   // Serialize to JSON string
    } catch (Exception e) {
        throw new AiApiException("Failed to build request body", e);
    }
}
```
- Programmatically constructs the JSON body using Jackson's node API. This is safer than string concatenation (no JSON injection risks).
- `var` is Java 10's *local variable type inference* — it's equivalent to writing `ObjectNode body = MAPPER.createObjectNode()`.

```java
private ChatResponse handleResponse(HttpResponse<String> response) {
    int status = response.statusCode();

    if (status == 401) throw new AiAuthException("Invalid OpenAI API key");
    if (status == 429) throw new AiRateLimitException("OpenAI rate limit exceeded");
    if (status >= 400) throw new AiApiException("OpenAI API error (" + status + "): " + response.body());
```
- HTTP status code classification. Error handlers are checked in a specific order:
  - `401` first (most specific auth error)
  - `429` next (most specific rate limit)
  - `>= 400` last (catches all other 4xx and 5xx)

```java
    JsonNode root = MAPPER.readTree(response.body());
    String content = root.at("/choices/0/message/content").asText();
    int tokensUsed = root.at("/usage/total_tokens").asInt();
    return new ChatResponse(content, model, tokensUsed);
```
- **`readTree`**: Parses the JSON response body into a `JsonNode` tree.
- **`root.at("/choices/0/message/content")`**: Uses a *JSON Pointer* (RFC 6901) to navigate the tree. `/choices/0/message/content` means: "go to key `choices`, then index `0`, then `message`, then `content`."
- This is the OpenAI API response structure:
  ```json
  {"choices": [{"message": {"content": "Hello!"}}], "usage": {"total_tokens": 42}}
  ```

---

## 8. GeminiChat.java — Google Gemini Provider

```java
private static final String ENDPOINT_TEMPLATE =
    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
```
- Gemini does NOT use a header for the API key. Instead, the key goes in the URL as a query parameter (`?key=...`). The `%s` placeholders are filled at runtime using `String.format(ENDPOINT_TEMPLATE, model, apiKey)`.

```java
var parts = content.putArray("parts");
parts.addObject().put("text", prompt);
```
- Gemini's request body structure is different from OpenAI's. It expects:
  ```json
  {"contents": [{"parts": [{"text": "Hello"}]}]}
  ```
- The nested array structure (`contents → parts`) is Gemini's way of supporting multi-modal inputs (text + images).

```java
String content = root.at("/candidates/0/content/parts/0/text").asText();
int tokensUsed = root.at("/usageMetadata/totalTokenCount").asInt();
```
- Gemini's response structure:
  ```json
  {"candidates": [{"content": {"parts": [{"text": "Hello!"}]}}], "usageMetadata": {"totalTokenCount": 35}}
  ```

---

## 9. AnthropicChat.java — Anthropic Claude Provider

```java
HttpRequest request = HttpRequest.newBuilder()
        ...
        .header("x-api-key", apiKey)
        .header("anthropic-version", "2023-06-01")
        ...
```
- Anthropic uses `x-api-key` instead of `Authorization: Bearer`.
- The `anthropic-version` header is **mandatory** — Anthropic uses it to maintain API backwards compatibility. We pin it to `"2023-06-01"` which is the stable version.

```java
body.put("max_tokens", 1024);
```
- Anthropic's API **requires** `max_tokens` in every request (unlike OpenAI where it's optional). We default to `1024` tokens which is sufficient for most responses.

```java
String content = root.at("/content/0/text").asText();
int inputTokens = root.at("/usage/input_tokens").asInt();
int outputTokens = root.at("/usage/output_tokens").asInt();
return new ChatResponse(content, model, inputTokens + outputTokens);
```
- Anthropic's response separates `input_tokens` and `output_tokens` — we sum them for the `tokensUsed` field.
- Anthropic response structure:
  ```json
  {"content": [{"text": "Hello!"}], "usage": {"input_tokens": 10, "output_tokens": 25}}
  ```

---

## 10. OllamaChat.java — Local Ollama Provider

```java
private static final String DEFAULT_HOST = "http://localhost:11434";

public OllamaChat(String model) {
    this(model, getOllamaHost());
}

private static String getOllamaHost() {
    String host = System.getenv("OLLAMA_HOST");
    return (host == null || host.isBlank()) ? DEFAULT_HOST : host;
}
```
- Ollama runs locally by default on port `11434`. Users can override the host via the `OLLAMA_HOST` environment variable — useful for running Ollama on a remote server or Docker container.

```java
if (host.endsWith("/")) {
    host = host.substring(0, host.length() - 1);
}
this.endpoint = host + "/api/chat";
```
- Defensive cleanup: if the user passes `"http://localhost:11434/"` (trailing slash), we strip it to prevent a malformed URL like `http://localhost:11434//api/chat`.

```java
body.put("stream", false);
```
- Ollama supports streaming responses by default. Setting `"stream": false` makes it return the complete response in a single HTTP response body, which is much simpler to handle.

```java
int promptEvalCount = root.at("/prompt_eval_count").asInt();
int evalCount = root.at("/eval_count").asInt();
return new ChatResponse(content, model, promptEvalCount + evalCount);
```
- Ollama's token counts:
  - `prompt_eval_count` — tokens in the input prompt
  - `eval_count` — tokens in the generated output

---

## 11. ModelRouter.java — The Request Router

```java
public class ModelRouter {

    public static Chat route(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model name cannot be empty");
        }
```
- **Null/blank guard**: Always validate inputs at the entry of a public method. Fail fast with a clear message rather than getting a confusing `NullPointerException` 5 calls deep.

```java
        String lowerModel = model.toLowerCase();
```
- Normalize the model name to lowercase before checking prefixes. This means `"GPT-4o"`, `"gpt-4o"`, and `"Gpt-4o"` all route correctly.

```java
        if (lowerModel.startsWith("gpt-") || lowerModel.startsWith("o1-") || lowerModel.startsWith("o3-")) {
            return new OpenAiChat(model);   // Note: original casing preserved
        } else if (lowerModel.startsWith("gemini-")) {
            return new GeminiChat(model);
        } else if (lowerModel.startsWith("claude-")) {
            return new AnthropicChat(model);
        } else {
            return new OllamaChat(model);   // Default: local model
        }
```
- **Prefix-based routing**:
  - OpenAI models always start with `gpt-`, `o1-`, or `o3-` (their new reasoning model series)
  - Google Gemini models always start with `gemini-`
  - Anthropic Claude models always start with `claude-`
  - Anything else (e.g. `"llama3"`, `"mistral"`, `"deepseek-coder"`) defaults to Ollama

---

## 12. AiClient.java — The Main Entry Point

This is the **Facade** — the single class a developer needs to know about.

```java
public class AiClient {
    private final String defaultModel;
    private final String openAiApiKey;
    private final String geminiApiKey;
    private final String anthropicApiKey;
    private final String ollamaHost;
```
- All fields are `final` — `AiClient` is immutable after construction (thread-safe).
- Stores per-provider API keys so the client can inject them into provider instances.

```java
    private AiClient(Builder builder) {
        this.defaultModel = builder.defaultModel;
        ...
    }
```
- **Private constructor** — enforces use of the `Builder`.

```java
    public String chat(String prompt) {
        if (defaultModel == null) {
            throw new IllegalStateException("Default model is not configured.");
        }
        return chat(defaultModel, prompt);
    }
```
- If `defaultModel` was set via the builder, the user can just call `.chat("Hello")` without specifying the model each time.

```java
    public static String chatQuick(String model, String prompt) {
        return ModelRouter.route(model).call(prompt);
    }
```
- **The most Python-like method in the library.** No instantiation needed. `ModelRouter` auto-selects the provider from environment variables, and `call()` returns just the text. This is inspired by how Python does it:
  ```python
  # Python: openai.chat.completions.create(...)
  # sidd-ai: AiClient.chatQuick("gpt-4o", "Hello")
  ```

```java
    private Chat getProvider(String model) {
        String lowerModel = model.toLowerCase();
        if (lowerModel.startsWith("gpt-") || ...) {
            return openAiApiKey != null
                ? new OpenAiChat(model, openAiApiKey)  // use programmatic key
                : new OpenAiChat(model);               // use env variable
        }
        ...
    }
```
- When using `AiClient` (not `chatQuick`), programmatic API keys take priority over environment variables.
- If no key was configured in the builder, it falls back to reading from the environment.

---

## 13. Usage Examples

### Quick one-liner (environment variable key)
```java
// Set env: GEMINI_API_KEY=your-key
String reply = AiClient.chatQuick("gemini-1.5-flash", "What is machine learning?");
System.out.println(reply);
```

### Using a configured client
```java
AiClient client = AiClient.builder()
    .defaultModel("gpt-4o")
    .openAiApiKey("sk-your-openai-key")
    .geminiApiKey("your-gemini-key")
    .build();

// Use the default model
String reply1 = client.chat("Explain recursion in one sentence.");

// Override the model
String reply2 = client.chat("claude-3-5-sonnet", "Summarize this text.");

// Get full metadata
ChatResponse response = client.chatResponse("gemini-1.5-flash", "Hello!");
System.out.println(response.getContent());
System.out.println("Tokens used: " + response.getTokensUsed());
```

### Using a specific provider directly
```java
// Direct provider usage (most control)
GeminiChat gemini = new GeminiChat("gemini-1.5-flash", "your-api-key");
String reply = gemini.call("Explain transformers in AI.");

// Or with the ChatRequest builder for fine-grained config
ChatRequest request = ChatRequest.builder()
    .prompt("Write a haiku about Java")
    .temperature(0.9)
    .maxTokens(100)
    .build();

String haiku = gemini.call(request);
```

### Using local Ollama
```java
// Ensure Ollama is running locally: `ollama serve`
OllamaChat local = new OllamaChat("llama3");
String reply = local.call("Tell me a fun fact about penguins.");

// Or via the quick static API
String reply2 = AiClient.chatQuick("llama3", "Tell me a joke.");
```

### Handling errors
```java
try {
    String reply = AiClient.chatQuick("gpt-4o", "Hello");
} catch (AiAuthException e) {
    System.err.println("API key is missing or invalid: " + e.getMessage());
} catch (AiRateLimitException e) {
    System.err.println("Rate limit hit. Retry after a delay.");
} catch (AiApiException e) {
    System.err.println("API error: " + e.getMessage());
}
```
