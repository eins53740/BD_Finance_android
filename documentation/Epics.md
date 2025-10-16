# Epics for BD_fiannce Android Client

## Epic 1: Core Market Data Intake and Evaluation

- Implement Yahoo Finance client for quotes, historical prices, and peer symbol discovery.
- Build StockEvaluator logic with deterministic thresholds and verdict calculation.
- Introduce repository caching with TTL and graceful degradation on partial failures.

## Epic 2: Investor Dashboard Experience

- Create Compose UI for ticker intake, loading, error, and success states.
- Surface summary, metrics, risk, momentum, peer comparison, and dividend cards.
- Render Mermaid decision flow locally via WebView with theming consistent with verdict state.

## Epic 3: AI Second Opinion and Content Fallback

- Integrate Groq (primary) and Gemini (fallback) LLM calls using BuildConfig supplied API keys.
- Convert Markdown responses into safe HTML and display within the app.
- Provide deterministic fallback narrative when LLM access fails or keys are absent.

## Epic 4: Quality, Observability, and Packaging

- Add logging and instrumentation to track fetch latency, cache hits, and evaluation outcomes.
- Document configuration requirements (API keys, caching parameters, timeouts).
- Produce signed and debug APK builds and ensure automated unit tests cover evaluator and repository logic.

