# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

BD Finance Android — a Jetpack Compose app that turns a stock ticker into an explainable Buy / Buy with Caution / Do Not Buy recommendation. Combines Yahoo Finance data, a deterministic rule engine, fundamental scorecards with intrinsic valuations, and optional Groq/Gemini AI commentary.

## Build & Run

```bash
# Build debug APK (JDK 17 required)
./gradlew.bat :app:assembleDebug

# Run all unit tests
./gradlew.bat testDebugUnitTest

# Run a single test class
./gradlew.bat testDebugUnitTest --tests "com.example.bd_finance.data.sync.StockMetricsAggregatorTest"

# Run a single test method
./gradlew.bat testDebugUnitTest --tests "com.example.bd_finance.data.sync.StockMetricsAggregatorTest.primary source metrics are used when available"

# Lint
./gradlew.bat lint

# Install on device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**SDK targets:** minSdk 33, targetSdk 36, compileSdk 36, Java 11 source/target, Kotlin 2.2.20.

## API Keys

Secrets resolve in order: `gradle.properties` → environment variables → `.env` file at project root. All are optional — the app falls back to deterministic analysis without them.

| Key | Service | Purpose |
|-----|---------|---------|
| `GROQ_API_KEY` | Groq (LLaMA 3.1 8B) | Primary LLM second opinion |
| `GEMINI_API_KEY` | Google Gemini | LLM fallback |
| `FMP_API_KEY` | Financial Modeling Prep | Sector medians, 5Y/10Y fundamentals |
| `ALPHA_VANTAGE_API_KEY` | Alpha Vantage | PEG, beta, sector fallback |

Keys are injected via `BuildConfig` fields in `app/build.gradle.kts`.

## Architecture

Single-module MVVM with manual dependency injection (no Hilt/Dagger). Entry point: `StockAnalysisRepository.default()` builds the full dependency graph.

### Data Flow

```
User enters ticker
  → StockEvaluatorViewModel (StateFlow: Idle→Loading→Success/Error)
    → StockAnalysisRepository (in-memory cache, 10min TTL)
      → YahooFinanceClient (quotes, history, peers via crumb/session auth)
      → StockEvaluator (deterministic verdict from EvaluationThresholds)
      → FundamentalAnalysisEngine (scorecard, DCF/Graham/DDM valuations)
      → LargeLanguageModelClient (Groq→Gemini→deterministic fallback)
    → StockAnalysis result rendered in Compose UI
```

### Background Sync (separate path)

```
WorkManager → DataRefreshWorker
  → StockMetricsAggregator (merges Yahoo + FMP + Alpha Vantage connectors)
    → StockMetricsRepository (Room DB, stock_metrics.db v3)
```

`BDFinanceApplication` initializes WorkManager with `StockMetricsWorkerFactory`. `StockMetricsSyncModule` acts as the service locator for sync infrastructure.

### Package Layout (`com.example.bd_finance`)

| Package | Responsibility |
|---------|---------------|
| `data/` | `StockAnalysisRepository` — orchestration and caching |
| `data/network/` | `YahooFinanceClient` — HTTP with crumb retry logic |
| `data/analysis/` | `StockEvaluator` — verdict rules, `MermaidDefinitionBuilder` |
| `data/fundamentals/` | `FundamentalAnalysisEngine`, `FundamentalScoringConfig`, `FundamentalConfigRegistry` |
| `data/llm/` | `LargeLanguageModelClient`, `PromptBuilder` |
| `data/model/` | Domain models: `StockAnalysis`, `StockVerdict`, `FundamentalInsights`, `IntrinsicValuation` |
| `data/sync/` | Multi-source aggregator, Room entities/migrations, WorkManager worker, connectors |
| `ui/` | `StockEvaluatorScreen` (Compose), `StockEvaluatorViewModel` |
| `ui/theme/` | Material 3 theme, colors, typography |

### Verdict Logic (`StockEvaluator`)

Thresholds evaluate P/E, PEG, momentum, beta, market cap, dividend coverage:
- 2+ FAILs → `DO_NOT_BUY`
- 1 FAIL + 1+ CLOSE_FAIL → `DO_NOT_BUY`
- 1 FAIL alone → `BUY_WITH_CAUTION`
- 2+ CLOSE_FAILs → `BUY_WITH_CAUTION`
- Otherwise → `BUY`

### Multi-Source Aggregation (`StockMetricsAggregator`)

Connectors implement a common interface; aggregator merges primary (Yahoo) with secondary (FMP) and tertiary (Alpha Vantage) sources, using fallback when primary fields are missing.

## Testing

Tests use JUnit 4 + Robolectric + coroutines-test + Room in-memory DB + OkHttp MockWebServer. Fakes/stubs are defined inline in test files (no shared test fixtures directory).

Test classes mirror source structure under `app/src/test/`:
- `data/fundamentals/FundamentalAnalysisEngineTest` — scoring engine with fake aggregator
- `data/fundamentals/FundamentalConfigOverridesTest` — runtime config overrides
- `data/sync/StockMetricsAggregatorTest` — multi-source merge with fake connectors
- `data/sync/StockMetricsRepositoryTest` — Room persistence
- `data/sync/DataRefreshWorkerTest` — WorkManager worker
- `data/sync/AlphaVantageSectorConnectorTest`, `FmpFundamentalConnectorTest` — connector mocks

## CI

GitHub Actions workflow at `.github/fs_workflow_fs/android-ci.yml`:
- Triggers on push/PR to main when `app/`, `gradle/`, or build files change
- Jobs: lint → unit tests → instrumented tests → build APKs → dependency security scan

## Conventions

- Commit messages: `<type>: <imperative summary>` (feat, fix, docs, refactor, test, chore)
- Small incremental PRs (target ≤200 LOC excluding tests)
- TDD approach — write tests first, AAA pattern
- Structured logging preferred over `println`
- `FundamentalScoringConfig` defaults: 60/40 sector/decade weight, z-score clamp ±3. Override via `FundamentalConfigOverrides.applyJson()` or `FundamentalConfigRegistry.replace()`
