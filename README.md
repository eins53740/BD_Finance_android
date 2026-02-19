# BD Finance Android

BD Finance is a Jetpack Compose Android client that turns any stock ticker into an explainable Buy / Sell / Hold recommendation. It wraps Yahoo Finance market data, a deterministic rule engine, and optional Groq/Gemini second-opinion commentary into one mobile dashboard produced by BD for friends, investors, and advisors who need a quick, transparent read on equities.

## Why Use It?
- **Deterministic Insights** - Fundamental thresholds produce a clear verdict ("Buy", "Buy with Caution", "Do Not Buy") with the entire decision trail, so you always know *why*.
- **Fundamental Scorecard** - Sector medians, 5Y/10Y history, profitability, stability, and growth trends roll into a single card alongside intrinsic value bands (DCF, Ben Graham, DDM).
- **Rich Context** - Key metrics, risk profiles, momentum, dividend health, peer comparisons, and a Mermaid decision flow are rendered together.
- **AI Commentary** - When Groq and/or Gemini API keys are supplied, the app adds qualitative guidance; otherwise it falls back to a deterministic narrative so you never see a blank panel.
- **Mobile-Ready** - Compose UI, Material 3 styling, dark-mode support, and caching for quick repeats keep analysis friendly on phones and tablets.

## Getting Started
1. **Prerequisites**
   - Android Studio Hedgehog (2023.1.1) or newer — the bundled JDK 17 is sufficient.
   - Alternatively: VS Code with Android extensions and a standalone JDK 17.
2. **Clone & Build**
   ```bash
   git clone <repo-url> BD_Finance_android
   cd BD_Finance_android
   cp .env.example .env   # then fill in any keys you have
   ./gradlew :app:assembleDebug
   ```
3. **Run** — Deploy from Android Studio (`Run > Run 'app'`) or install `app/build/outputs/apk/debug/app-debug.apk`.
4. **Optional API Keys** — All keys are optional. The app works fully without them,
   falling back to deterministic analysis and cached data.

   Create a `.env` file at the project root (see `.env.example`):
   ```bash
   GROQ_API_KEY=gsk_...      # Groq LLaMA — primary LLM commentary
   GEMINI_API_KEY=AIza...     # Google Gemini — LLM fallback
   FMP_API_KEY=fnm_...        # Financial Modeling Prep — sector medians
   ALPHA_VANTAGE_API_KEY=...  # Alpha Vantage — PEG, beta fallback
   ```

   Secrets resolve in order: `gradle.properties` → environment variables → `.env` file.
   **Never commit real keys to `gradle.properties`** — use `.env` (git-ignored) instead.

## Changing the App Icon
1. Prepare a square 512x512 PNG (transparent background recommended).
2. Replace the existing files under `app/src/main/res/mipmap-*/ic_launcher.png` and `ic_launcher_round.png`. The fastest method is to use **Android Studio > Image Asset**:
   - Right-click `app` > **New > Image Asset**.
   - Choose **Launcher Icons (Adaptive and Legacy)**.
   - Select your new image and let Studio regenerate every mipmap density.
3. Sync Gradle and rebuild (`./gradlew :app:assembleDebug`) to verify the new icon is packaged.

## Project Structure
- **app/src/main/java/com/example/bd_finance/** - Root package for UI, data, networking, evaluation logic.
- **data/network/YahooFinanceClient** - Fetches quotes, history, peer data with Yahoo crumb/session handling.
- **data/analysis/StockEvaluator** - Deterministic rule engine and Mermaid flow generator.
- **ui/** - Compose UI, ViewModel, theming.
- **documentation/** - Product brief, epics, task breakdown.
- **README.md** - You are here.

## Author
Created by **BD** - a retail-investor-friendly tool shared with friends who want guardrails and context before acting on tickers.

## Contributing / Feedback
Issues and improvements are welcome via pull requests. When reporting bugs, include the ticker, device/emulator, and console logs (especially around Yahoo/Groq/Gemini calls) so we can reproduce quickly.

Enjoy the insights and happy investing!

## Fundamental Scorecard Tuning
- The weighting, z-score clamp, and valuation assumptions used by the new scorecard default to 60/40 (sector/decade) with a clamp of +/- 3.
- To override at runtime, call `FundamentalConfigOverrides.applyJson(jsonString)` (for example from a Remote Config fetch or debug menu) with fields like `sectorWeight`, `historyWeight`, `zScoreClamp`, `pegTarget`, `dcfDiscountFloor`, etc.
- Current overrides are stored in-memory via `FundamentalConfigRegistry` and immediately applied to subsequent analyses; calling `FundamentalConfigRegistry.replace(FundamentalScoringConfig())` resets to defaults.

## Background Sync & Data Sources
- Yahoo Finance remains the primary live quote provider.
- Alpha Vantage acts as a free fallback for PEG, beta, and supplementary fundamentals (set `ALPHA_VANTAGE_API_KEY` in `.env`).
- Financial Modeling Prep (set `FMP_API_KEY` in `.env`) unlocks sector medians and 5Y/10Y scorecard history with Alpha Vantage fallback when quotas are exhausted.
- WorkManager schedules periodic refreshes that hydrate a Room-backed metrics cache consumed by the Compose UI.
