# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BD Finance is a Jetpack Compose Android application that provides explainable Buy/Sell/Hold stock recommendations. It combines Yahoo Finance market data, a deterministic rule engine, and optional AI commentary (Groq/Gemini) into a mobile dashboard for equity analysis.

## Build & Development Commands

### Building the Application
```bash
# Build debug APK
./gradlew :app:assembleDebug

# Build release APK
./gradlew :app:assembleRelease

# Clean build
./gradlew clean
```

### Running Tests
```bash
# Run all unit tests
./gradlew :app:test

# Run specific test class
./gradlew :app:test --tests "com.example.bd_finance.SpecificTestClass"

# Run instrumented tests (requires emulator/device)
./gradlew :app:connectedAndroidTest
```

### Code Quality
```bash
# Check for lint issues
./gradlew :app:lint

# Generate lint report
./gradlew :app:lintDebug
```

### Installation
```bash
# Install debug APK to connected device
./gradlew :app:installDebug
```

## API Configuration

The app supports three methods for providing API keys (in priority order):

1. **`.env` file** (recommended for local development) - Create at project root:
   ```
   GROQ_API_KEY=sk-...
   GEMINI_API_KEY=AIza...
   FMP_API_KEY=fnm_...
   ALPHA_VANTAGE_API_KEY=...
   ```

2. **Environment variables** - Set in your shell or IDE run configuration

3. **`gradle.properties`** - Add to `~/.gradle/gradle.properties` or project root

**Note:** All API keys are optional. The app falls back to deterministic narratives when keys are missing. Sector medians and historical fundamentals use cached data when FMP is unavailable.

## Architecture Overview

### Data Layer Architecture

**Network Clients:**
- `YahooFinanceClient.kt` - Primary quote provider with crumb/session authentication
  - Fetches quotes, historical prices, peer symbols
  - Handles dynamic authentication tokens
- `AlphaVantageMetricsConnector.kt` - Fallback for PEG, beta, supplementary fundamentals
- `AlphaVantageSectorConnector.kt` - Sector-level data aggregation
- `FmpFundamentalConnector.kt` - Financial Modeling Prep integration for sector medians and 5Y/10Y history
- `YahooFinanceMetricsConnector.kt` - Additional Yahoo Finance metrics
- `LargeLanguageModelClient.kt` - Groq/Gemini API integration for AI commentary

**Repositories:**
- `StockAnalysisRepository.kt` - Orchestrates stock analysis by coordinating YahooFinanceClient, StockEvaluator, and LLM clients
- `StockMetricsRepository.kt` - Manages Room-backed metrics cache for WorkManager sync results

**Data Models:**
- `AnalysisModels.kt` - Comprehensive models for quotes, historical data, analysis results, decision metrics
- `NormalizedStockMetrics.kt` - Normalized metrics across multiple data sources

### Evaluation & Analysis Engine

**Core Components:**
- `StockEvaluator.kt` - Deterministic rule engine that produces Buy/Buy with Caution/Do Not Buy verdicts
  - Generates Mermaid decision flow diagrams showing the complete reasoning chain
  - Performs multi-dimensional checks: P/E valuation, PEG ratio, momentum, beta, market cap, dividend sustainability
- `FundamentalAnalysisEngine.kt` - Advanced scorecard system
  - Computes sector-relative and decade-relative z-scores
  - Integrates intrinsic valuation models (DCF, Ben Graham, DDM)
  - Aggregates profitability, stability, growth metrics
- `FundamentalConfig.kt` - Runtime-tunable configuration for scoring weights and thresholds
  - Use `FundamentalConfigOverrides.applyJson(jsonString)` to override defaults
  - Supports remote config integration via `FundamentalConfigRegistry`

### Background Synchronization

**WorkManager Setup:**
- `DataRefreshWorker.kt` - Periodic background worker that fetches and caches metrics
- `StockMetricsSyncScheduler.kt` - Configures sync intervals and constraints
- `StockMetricsWorkerFactory.kt` - Custom DI for workers
- `StockMetricsSyncModule.kt` - Dependency injection module for sync infrastructure
- `StockMetricsAggregator.kt` - Merges data from multiple connectors with priority fallbacks
- `TickerProvider.kt` - Defines which tickers to sync

**Initialization:**
Background sync is initialized in `BDFinanceApplication.onCreate()` via `StockMetricsSyncScheduler`.

### UI Architecture

**Compose Structure:**
- `MainActivity.kt` - Main entry point and navigation root
- `StockEvaluatorScreen.kt` - Primary UI displaying:
  - Stock quote card
  - Fundamental scorecard with sector/decade comparison
  - Intrinsic valuation bands
  - Decision flow (Mermaid diagram rendered as text)
  - AI commentary panel (or deterministic fallback)
  - Risk profile, momentum, dividend health, peer comparisons
- `StockEvaluatorViewModel.kt` - Manages UI state, coordinates repository calls, handles loading/error states

**Theming:**
- Material 3 design system
- Dark mode support
- Custom color schemes in `ui/theme/Color.kt`
- Typography definitions in `ui/theme/Type.kt`

## Key Dependencies

- **Kotlin Coroutines** - Asynchronous programming
- **OkHttp** - HTTP client for all network operations
- **Room** - Local database for metrics caching
- **WorkManager** - Background data synchronization
- **Jetpack Compose** - Modern declarative UI
- **Material 3** - Design system and components

## Development Notes

### Project Structure
```
app/src/main/java/com/example/bd_finance/
├── BDFinanceApplication.kt    # App initialization, WorkManager setup
├── MainActivity.kt             # Main activity and nav root
├── data/
│   ├── StockAnalysisRepository.kt
│   ├── analysis/
│   │   └── StockEvaluator.kt   # Core rule engine
│   ├── fundamentals/
│   │   ├── FundamentalAnalysisEngine.kt
│   │   └── FundamentalConfig.kt
│   ├── llm/
│   │   └── LargeLanguageModelClient.kt
│   ├── model/
│   │   └── AnalysisModels.kt
│   ├── network/
│   │   └── YahooFinanceClient.kt
│   └── sync/                   # WorkManager, connectors, metrics aggregation
│       ├── DataRefreshWorker.kt
│       ├── StockMetricsRepository.kt
│       ├── StockMetricsSyncScheduler.kt
│       └── [various connectors]
└── ui/
    ├── StockEvaluatorScreen.kt
    ├── StockEvaluatorViewModel.kt
    └── theme/
```

### Fundamental Scorecard Tuning

The scorecard defaults to 60% sector-relative / 40% decade-relative weighting with z-score clamping at ±3. To override at runtime:

```kotlin
// From remote config or debug menu
FundamentalConfigOverrides.applyJson("""
{
  "sectorWeight": 0.7,
  "historyWeight": 0.3,
  "zScoreClamp": 2.5,
  "pegTarget": 1.2,
  "dcfDiscountFloor": 0.08
}
""")

// Reset to defaults
FundamentalConfigRegistry.replace(FundamentalScoringConfig())
```

### Yahoo Finance Crumb Authentication

`YahooFinanceClient` automatically handles Yahoo's authentication flow:
1. Fetches cookies from the main page
2. Extracts a crumb token
3. Includes both in subsequent API requests

This is transparent to callers but may require updates if Yahoo changes their authentication scheme.

### AI Commentary Fallback

When `GROQ_API_KEY` and `GEMINI_API_KEY` are absent or API calls fail:
- `LargeLanguageModelClient` returns deterministic narratives
- UI displays analysis summary without external AI dependency
- No blank panels or error states visible to users

### Testing Strategy

**Unit Tests:**
- Located in `app/src/test/`
- Use MockWebServer for network client tests
- Robolectric for Android framework dependencies
- Focus on `StockEvaluator`, `FundamentalAnalysisEngine`, connectors

**Test Dependencies:**
- JUnit 4
- Kotlinx Coroutines Test
- OkHttp MockWebServer
- Robolectric
- Room Testing
- WorkManager Testing

### Minimum Requirements

- **Android SDK**: Min 33, Target 36
- **JDK**: 11 (configured in app/build.gradle.kts)
- **Android Studio**: Giraffe or newer
- **Gradle**: Uses wrapper (./gradlew or gradlew.bat)

### Common Workflows

**Adding a New Data Source:**
1. Create connector in `data/sync/` (e.g., `NewProviderConnector.kt`)
2. Implement `fetchMetricsFor(symbol: String): NormalizedStockMetrics?`
3. Register in `StockMetricsAggregator` with priority order
4. Add API key handling in `app/build.gradle.kts` if needed

**Modifying Evaluation Logic:**
1. Update thresholds or decision rules in `StockEvaluator.kt`
2. Adjust `FundamentalConfig.kt` for scorecard parameters
3. Regenerate Mermaid flow logic in `StockEvaluator.generateMermaidFlow()`
4. Add unit tests for new decision paths

**Updating UI:**
1. Modify `StockEvaluatorScreen.kt` for layout changes
2. Update `StockEvaluatorViewModel.kt` for state management
3. Test with both light and dark themes
4. Verify Material 3 design guidelines

### Icon Replacement

To update the app icon:
1. Prepare 512x512 PNG
2. Android Studio > Right-click `app` > New > Image Asset
3. Select Launcher Icons (Adaptive and Legacy)
4. Let Studio regenerate all mipmap densities
5. Rebuild: `./gradlew :app:assembleDebug`
