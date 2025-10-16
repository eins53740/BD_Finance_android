# Stock Evaluator Android Client – Product Requirements

## Product Summary

- Android client accepts a stock ticker and surfaces a comprehensive evaluation dashboard tailored to non institutional investors.
- Stock fundamentals are fetched through Yahoo Finance endpoints with 10 minute TTL caching to balance freshness with API rate limits.
- The StockEvaluator engine applies deterministic financial thresholds to return Buy, Buy with Caution, or Do Not Buy outcomes plus the decision trail.
- Results view surfaces tabular metrics, risk score, momentum, peer comparisons, dividend insights, and a Mermaid flowchart rendered in app via WebView.
- A large language model second opinion (Groq preferred, Gemini fallback) delivers markdown formatted qualitative commentary rendered as HTML.

## Goals

- Provide transparent buy or sell recommendations with explainable intermediate decisions and raw metrics.
- Keep response time under four seconds for 95 percent of tickers through caching, fallback data sources, and async friendly design.
- Offer a single mobile surface that aligns with the web service layer without duplicating business logic.
- Surface auxiliary analytics (risk, momentum, comparative, dividend) that contextualize the core verdict.
- Deliver consistent AI commentary while handling provider outages gracefully.

## Success Metrics

- 95th percentile end to end response time under four seconds from ticker submission to rendered screen.
- Fewer than one percent of requests end in unhandled errors or missing mandatory sections.
- 80 percent of completed analyses display both the flowchart and AI opinion without manual refresh.
- At least 90 percent cache hit rate for repeat tickers within TTL windows once usage is steady.

## Personas

- Retail Investor: Self directed investor seeking a quick pulse on popular equities and rationale for action.
- Financial Advisor: Needs a defensible screening aid that can be shared with clients to illustrate decision flows.
- Product Owner: Responsible for parity between the web app and Android client leveraging the same service layer.

## User Journey

1. User enters a ticker (for example AAPL) and submits while the UI shows a spinner.
2. System normalizes ticker, validates presence, and leverages cached market data or fetches from Yahoo Finance with fallbacks.
3. StockEvaluator runs sequential metric checks, recording pass or fail states and active links, while StockAnalysisFeatures derives advanced insights.
4. If configured, the system prompts an LLM for a secondary opinion and converts markdown to HTML.
5. UI renders the decision, metrics table, flowchart definition, AI narrative, and auxiliary analytics; errors surface if retrieval fails.

Mermaid sequence:

```
flowchart LR
    User -->|Ticker Form| MobileUI
    MobileUI -->|Analyze| AnalysisService
    AnalysisService -->|Fundamentals| YFinance
    AnalysisService --> StockEvaluator
    StockEvaluator -->|Verdict & Path| AnalysisService
    AnalysisService --> StockFeatures
    AnalysisService -->|Prompt| LLM
    LLM -->|Opinion HTML| AnalysisService
    AnalysisService -->|Context| MobileUI
    MobileUI -->|Mermaid Definition| FlowchartRenderer
```

## Functional Requirements

### Ticker Intake

- Upper case normalization, empty input validation, and user facing messaging with ticker echo on failure.

### Market Data Pipeline

- Pull get quote data, fall back to historical price endpoints, and raise actionable errors when no data is returned.

### Evaluation Engine

- Execute ordered metric checks with pass or close fail states, compute verdict, and expose the decision path plus active links.

### Analytics Surfaces

- Deliver risk assessment, trend momentum, peer comparisons, and dividend sustainability analysis.

### Visualization Delivery

- Generate Mermaid definition strings, render via WebView with animations and ensure responsive layout.

### AI Opinion

- Build investor style prompt, prefer Groq with configurable model, fall back to Gemini, convert markdown to HTML, and suppress section when providers unavailable.

## Non Functional Requirements

- **Performance:** Cache up to 100 ticker responses for 10 minutes, avoid blocking requests, and reuse initialized services.
- **Reliability:** Clear cached entries after exceptions, log warnings for partial data retrieval, and degrade gracefully when LLMs or metrics are missing.
- **Security:** Keep API keys outside of logs or source control, read from configuration, and guard secrets in BuildConfig.
- **Observability:** Use structured logging and expose evaluation timings for supportability.
- **Portability:** Business logic remains platform agnostic to simplify reuse.

## Dependencies

- Yahoo Finance APIs for stock fundamentals, historical pricing, and dividend history.
- cachetools like behavior implemented in repository layer for TTL caching.
- Mermaid.js delivered via WebView.
- LLM providers via OpenAI compatible Groq SDK and optional Google Gemini SDK.
- Jetpack Compose UI stack with Material 3.

## Risks

- Market data endpoints may throttle or change schema; mitigated by caching, fallbacks, and warning logs.
- Missing API keys or invalid credentials can suppress AI opinion, requiring configuration checks and user messaging.
- LLM latency or outages could delay responses; enforce timeouts and asynchronous design.
- Financial thresholds may be too rigid; schedule periodic reviews or allow configuration in future milestones.
- Mermaid rendering failures on older devices; add textual fallback.

## Future Android Work

- Expose REST endpoints sharing StockAnalysis payload structure for other mobile clients.
- Package flowchart definition and analytics in transport friendly JSON.
- Align UI components with Material design while preserving recommendation semantics.
- Implement offline caching strategies leveraging TTL assumptions and error messaging.
- Define feature flags for AI opinion and advanced analytics to handle connectivity variability.

