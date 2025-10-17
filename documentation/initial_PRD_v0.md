Create an apk name "BD_fiannce":
The application receives a stock ticker as input, analyzes the stock’s performance, and provides a recommendation — Buy, Sell, or Hold (Neutral). It also generates a Mermaid diagram illustrating the decision process and uses a Large Language Model (LLM) to provide a second opinion on the recommendation.


use this PRD as the foundation for building an Android APK version of the application.

PRD:
Stock Evaluator
Product Requirements Document for FlowchartStocks/stock-evaluator Flask web application powering ticker-driven stock analysis and recommendations.

Product Summary

Flask interface accepts a stock ticker and serves a comprehensive evaluation dashboard tailored to non-institutional investors.
Stock fundamentals are fetched through yfinance with 10-minute TTL caching to balance freshness with API rate limits.
The StockEvaluator engine applies deterministic financial thresholds to return Buy, Buy with Caution, or Do Not Buy outcomes plus the decision trail.
Results view surfaces tabular metrics, risk score, momentum, peer comparisons, dividend insights, and an interactive Mermaid flowchart driven by custom static/flowchart.js.
A large-language-model second opinion (Groq-preferred, Gemini fallback) delivers Markdown-formatted qualitative commentary rendered as HTML.
Goals

Provide transparent buy/sell recommendations with explainable intermediate decisions and raw metrics.
Keep response time under four seconds for 95% of tickers through caching, fallback data sources, and async-friendly design.
Offer a single web surface that can later be wrapped or queried by an Android APK without duplicating business logic.
Surface auxiliary analytics (risk, momentum, comparative, dividend) that contextualize the core verdict.
Deliver consistent AI commentary while handling provider outages gracefully.
Success Metrics

95th percentile end-to-end response time under four seconds from form submission to rendered page.
Less than 1% of requests end in unhandled errors or missing mandatory sections.
80% of completed analyses display both the flowchart and AI opinion without manual refresh.
At least 90% cache hit rate for repeat tickers within TTL windows once platform receives steady usage.
Personas
Retail Investor — Self-directed investor seeking a quick pulse on popular equities and rationale for action.
Financial Advisor — Needs a defensible screening aid that can be shared with clients to illustrate decision flows.
Product Owner — Responsible for parity between the Flask app and forthcoming Android client leveraging the same service layer.

User Journey

User enters a ticker (e.g., AAPL) and submits the form while the UI shows a spinner.
Server normalizes ticker, validates presence, and leverages cached market data or fetches from yfinance with fallbacks.
StockEvaluator runs sequential metric checks, recording pass/fail states and active links, while StockAnalysisFeatures derives advanced insights.
If configured, the system prompts an LLM for a secondary opinion and converts Markdown to HTML.
Flask renders the template with metrics table, flowchart definition, AI narrative, and auxiliary analytics; errors are flashed if retrieval fails.
System Overview

Client Layer Responsive Jinja2 template with loading states, flash messaging, and sections for decision, metrics, and AI content.
Service Layer StockAnalysisService orchestrates data retrieval, evaluation, flowchart generation, opinion synthesis, and payload assembly.
Decision Logic StockEvaluator encapsulates financial thresholds, tolerance for “close fail,” and link tracking for visualization.
Analytics Toolkit StockAnalysisFeatures augments core metrics with risk, trend, comparative, and dividend analyses using yfinance histories.
Visualization Runtime Custom flowchart.js wraps Mermaid to render diagrams, animate nodes, inject tooltips, and overlay risk indicators.
flowchart LR
    User -->|Ticker Form| FlaskUI
    FlaskUI -->|POST /| AnalysisService
    AnalysisService -->|Fundamentals| YFinance
    AnalysisService --> StockEvaluator
    StockEvaluator -->|Verdict & Path| AnalysisService
    AnalysisService --> StockFeatures
    AnalysisService -->|Prompt| LLM
    LLM -->|Opinion HTML| AnalysisService
    AnalysisService -->|Context| FlaskUI
    FlaskUI -->|Mermaid Definition| FlowchartJS
Functional Requirements

Ticker Intake Upper-case normalization, empty-input validation, and user-facing flash messaging with stateful ticker echo on failure.
Market Data Pipeline Pull Ticker.get_info, fall back to fast_info and five-day history, and raise actionable errors when no data is returned.
Evaluation Engine Execute ordered metric checks with pass/close fail/fail states, compute verdict (BUY, BUY with Caution, Do Not Buy), and expose the decision path plus active links.
Analytics Surfaces Deliver risk assessment (weighted scores and recommendations), trend momentum, peer comparisons, and dividend sustainability analysis.
Visualization Delivery Generate Mermaid definition strings, render via flowchart.js with animations, tooltips, risk badges, and ensure responsive layout.
AI Opinion Build investor-style prompt, prefer Groq (OpenAI-compatible) with configurable model, fall back to Gemini, convert Markdown to HTML, and suppress section when providers unavailable.
Non-Functional

Performance Cache up to 100 ticker responses for 10 minutes, avoid blocking requests, and reuse initialized services for multiple clients.
Reliability Clear cached entries after exceptions, log warnings for partial data retrieval, and degrade gracefully when LLMs or metrics are missing.
Security Guard Flask with configurable SECRET_KEY, read API keys from repo config or environment, and avoid logging sensitive keys or prompts.
Observability Use console logging in Python and enhanced diagnostics in flowchart.js (render timers, error logs, tooltip debugging) for supportability.
Portability Keep business logic in core package with no Flask dependencies to simplify reuse by API backends and the Android app.
Dependencies

yfinance for stock fundamentals, fast info, historical pricing, and dividend history.
cachetools TTLCache for memoizing ticker payloads and reducing network calls.
Mermaid.js plus custom static/flowchart.js for interactive flowchart rendering and instrumentation.
LLM providers via OpenAI-compatible Groq SDK and optional Google Gemini SDK with Markdown conversion.
Flask with Jinja2 templates, flash messaging, static asset pipeline, and WSGI-ready server.
Risks

Market data APIs can throttle or change schema; mitigated by caching, fallbacks, and warning logs.
Missing API keys or invalid credentials can suppress AI opinion, requiring configuration checks and user messaging.
LLM latency or outages could delay responses; enforce timeouts and asynchronous-ready design for future optimization.
Financial thresholds may be too rigid or stale; schedule periodic reviews or allow configuration in future milestones.
Mermaid rendering failures in older browsers can break visualization; maintain feature detection and provide textual fallback.
Future Android

Expose REST/GraphQL endpoints sharing StockAnalysisService payload structure for mobile clients.
Package flowchart definition and analytics in transport-friendly JSON, deferring rendering to the Android layer.
Align UI components with material design while preserving recommendation semantics and terminology.
Implement offline caching strategies on mobile leveraging the same TTL assumptions and error messaging.
Define feature flags for AI opinion and advanced analytics to handle varying mobile connectivity and quotas.
Data Pipeline

Implement yfinance integration with retries and structured logging for get_info, fast_info, and history lookups.
Normalize ticker symbols and enrich payloads with canonical longName, ticker, and price context.
Configure cachetools TTLCache size/ttl parameters and expose cache-clearing hooks for error recovery.
Instrument data fetch duration and cache hit ratios for future monitoring dashboards.
Document API key storage requirements and environment variable fallbacks for data providers.
Evaluation Engine

Extract metrics map with null-safe defaults, unit conversions, and validation for negative or zero values.
Encode threshold table with close-call tolerance and allow overrides for future customization.
Persist ordered decision path entries (name, observed, threshold, status) for UI rendering and auditing.
Track active flowchart links set for Mermaid styling to highlight traversed branches.
Cover evaluation scenarios with tests across PASS, CLOSE_FAIL, and FAIL to guarantee determinism.
Analytics Toolkit

Build volatility, liquidity, financial, market, and sector risk sub-scores with weighted aggregation and recommendations.
Compute multi-period trend metrics (1mo, 3mo, 6mo, 1y), returns, directions, and momentum scoring.
Produce sector/industry comparisons including valuation, growth, and profitability relative assessments.
Analyze dividend history for yield attractiveness, payout sustainability, and growth consistency.
Surface errors for unavailable data while keeping overall analysis resilient and user-informative.
Visualization UX

Design responsive template sections for verdict badge, metrics table, risk/trend/comparative/dividend cards, and AI report.
Implement spinner states, disabled submit button, and flash error banners to guide user interactions.
Render Mermaid diagrams with animation, tooltips, and link styling based on decision path and active links.
Overlay risk indicator badges on flowchart containers derived from evaluation outcomes.
Ensure accessibility with color-contrast aware theming, ARIA considerations, and dark-mode CSS variables.
AI Opinion

Craft investor-style prompt including formatted metrics and required commentary sections.
Integrate Groq API via OpenAI client compatibility with configurable model, max tokens, and temperature.
Introduce Gemini fallback with lazy import, client configuration, and structured response parsing.
Convert Markdown paragraphs into HTML using optional markdown library and fallback <pre> rendering.
Handle provider exceptions silently while logging diagnostic messages and omitting the AI section when unavailable.
Platform Enablement

Encapsulate stock analysis payload in reusable serializers for REST endpoints and Android consumption.
Centralize configuration management (env vars, .env, repo config) and document deployment prerequisites.
Maintain Docker assets, requirements.txt, and developer guides for consistent local and cloud execution.
Add automated tests for service layer, templates, and JS helpers, integrating into CI.
Plan API schema versioning, authentication, and rate limiting for the Android rollout.