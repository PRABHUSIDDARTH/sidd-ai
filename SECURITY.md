# Security Policy

## Supported Versions

`sidd-ai` is currently distributed as a single active line of releases.
Security fixes are applied to the latest published version on Maven
Central / GitHub Packages.

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                 |

## Reporting a Vulnerability

If you discover a security vulnerability in `sidd-ai` (for example, unsafe
handling of API keys, credential leakage in logs, or an injection issue in
request construction), please **do not open a public issue**.

Instead, report it privately using one of the following:

1. **GitHub Security Advisories** (preferred): go to the repository's
   **Security** tab → **Advisories** → **Report a vulnerability**.
2. **Email**: contact the maintainer directly (see the email/contact link
   in the repository README or GitHub profile).

Please include:

* A description of the vulnerability and its potential impact
* Steps to reproduce, or a proof-of-concept if available
* The version of `sidd-ai` affected

## What to Expect

* You'll receive an acknowledgment within a few days of your report.
* The maintainer will investigate and, if confirmed, work on a fix and
  coordinate a release timeline with you.
* Credit will be given in the release notes unless you prefer to remain
  anonymous.

## Scope

This policy covers the `sidd-ai` library code itself. It does not cover
vulnerabilities in third-party AI provider APIs (OpenAI, Gemini,
Anthropic, Ollama) that `sidd-ai` calls into — please report those
directly to the respective provider.
