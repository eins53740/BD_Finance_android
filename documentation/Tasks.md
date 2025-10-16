# Task Breakdown

## Epic 1 – Core Market Data Intake and Evaluation

1. Implement OkHttp based YahooFinanceClient to fetch quote, history, and peer symbols.
2. Define domain models for quotes, metrics, risk insights, momentum, dividend, and peers.
3. Build StockEvaluator with deterministic thresholds, verdict calculation, and Mermaid definition builder.
4. Introduce StockAnalysisRepository with TTL cache, concurrency management, and fallback handling.

## Epic 2 – Investor Dashboard Experience

1. Replace template MainActivity content with StockEvaluatorRoot composable.
2. Implement ViewModel exposing `StateFlow<StockEvaluatorUiState>` and handle retry logic.
3. Create Compose UI components for ticker intake, loading, error, summary, metrics, risk, momentum, peer, dividend, and AI sections.
4. Integrate WebView based Mermaid renderer and ensure HTML rendering for AI opinion.

## Epic 3 – AI Second Opinion and Content Fallback

1. Build LargeLanguageModelClient with Groq primary call, Gemini fallback, and Markdown to HTML conversion.
2. Add BuildConfig entries and gradle.properties placeholders for GROQ_API_KEY and GEMINI_API_KEY.
3. Surface fallback narrative when API keys or providers are unavailable.
4. Display helpful messaging in UI when no AI opinion is available.

## Epic 4 – Quality, Observability, and Packaging

1. Add structured logging for data fetch failures, cache usage, and LLM errors.
2. Document configuration, build, and troubleshooting steps.
3. Create instrumentation hooks for future latency tracking.
4. Automate Gradle assembleDebug and unit tests in CI once JAVA_HOME is configured.

