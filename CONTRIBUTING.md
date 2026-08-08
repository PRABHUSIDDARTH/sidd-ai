# Contributing to sidd-ai

Thanks for your interest in contributing to `sidd-ai` — a lightweight,
framework-agnostic Java SDK for calling AI models (OpenAI, Gemini,
Anthropic, Ollama) through a unified API. Contributions of all sizes are
welcome, from typo fixes to new provider integrations.

## Before You Start

* Please read the [Code of Conduct](CODE_OF_CONDUCT.md) — participation in
  this project means agreeing to follow it.
* For anything beyond a small fix, open an issue first to discuss the
  change before investing time in a PR. This avoids duplicate work and
  makes sure the change fits the project's direction.

## Getting Set Up

1. Fork the repository and clone your fork:
   ```bash
   git clone https://github.com/<your-username>/sidd-ai.git
   cd sidd-ai
   ```
2. Build the project (Maven):
   ```bash
   mvn clean install
   ```
3. Run the test suite to confirm everything passes before you start:
   ```bash
   mvn test
   ```

## Making Changes

* Create a feature branch off `main`:
  ```bash
  git checkout -b feature/short-description
  ```
* Keep changes focused — one logical change per PR.
* Match the existing code style (formatting, naming conventions, package
  structure).
* Add or update unit tests for any behavior you change.
* Update the README or Javadoc if you change public API surface.

## Commit Messages

Use clear, descriptive commit messages, e.g.:
```
Add support for streaming responses in OllamaClient
Fix NPE when API key is missing for AnthropicProvider
```

## Submitting a Pull Request

1. Push your branch and open a PR against `main`.
2. Fill out the PR template completely — description, motivation, and
   testing done.
3. Link any related issue (e.g. `Closes #12`).
4. Make sure CI passes and `mvn test` is green locally.
5. Be responsive to review feedback — a maintainer will review as soon as
   possible.

## Reporting Bugs / Requesting Features

Please use the issue templates under **Issues → New Issue** so we get the
information needed to reproduce or evaluate the request.

## Adding a New Provider

If you're adding support for a new AI provider:

* Follow the existing provider interface/abstraction used by
  OpenAI/Gemini/Anthropic/Ollama implementations.
* Include unit tests that don't require live network calls (mock the HTTP
  layer).
* Document required configuration (API keys, base URLs) in the README.

## Questions

If anything here is unclear, open a discussion or an issue — happy to
help you get started.
