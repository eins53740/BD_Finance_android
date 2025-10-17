# BD Finance Android

BD Finance is a Jetpack Compose Android client that turns any stock ticker into an explainable Buy / Sell / Hold recommendation. It wraps Yahoo Finance market data, a deterministic rule engine, and optional Groq/Gemini second-opinion commentary into one mobile dashboard produced by BD for friends, investors, and advisors who need a quick, transparent read on equities.

## Why Use It?
- **Deterministic Insights** – Fundamental thresholds produce a clear verdict (“Buy”, “Buy with Caution”, “Do Not Buy”) with the entire decision trail, so you always know *why*.
- **Rich Context** – Key metrics, risk profiles, momentum, dividend health, peer comparisons, and a Mermaid decision flow are rendered together.
- **AI Commentary** – When Groq and/or Gemini API keys are supplied, the app adds qualitative guidance; otherwise it falls back to a deterministic narrative so you never see a blank panel.
- **Mobile-Ready** – Compose UI, Material 3 styling, dark-mode support, and caching for quick repeats keep analysis friendly on phones and tablets.

## Getting Started
1. **Prerequisites** – Android Studio Giraffe+ (or VS Code w/ Android extensions) and JDK 17/21. The project includes Gradle wrapper and uses the Android Studio bundled JBR.
2. **Clone & Build**
   ```bash
   git clone <repo-url> BD_Finance_android
   cd BD_Finance_android
   ./gradlew :app:assembleDebug
   ```
3. **Run** – Deploy from Android Studio (`Run > Run 'app'`) or install `app/build/outputs/apk/debug/app-debug.apk`.
4. **Optional API Keys** – Add the following to `gradle.properties` (or via Android Studio Run Config):
   ```properties
   GROQ_API_KEY=sk-...
   GEMINI_API_KEY=AIza...
   ```
   Leaving them blank simply triggers the deterministic fallback copy in the Second Opinion section.

## Changing the App Icon
1. Prepare a square 512×512 PNG (transparent background recommended).
2. Replace the existing files under `app/src/main/res/mipmap-*/ic_launcher.png` and `ic_launcher_round.png`. The fastest method is to use **Android Studio > Image Asset**:
   - Right-click `app` ▶ **New ▶ Image Asset**.
   - Choose **Launcher Icons (Adaptive and Legacy)**.
   - Select your new image and let Studio regenerate every mipmap density.
3. Sync Gradle and rebuild (`./gradlew :app:assembleDebug`) to verify the new icon is packaged.

## Project Structure
- **app/src/main/java/com/example/bd_finance/** – Root package for UI, data, networking, evaluation logic.
- **data/network/YahooFinanceClient** – Fetches quotes, history, peer data with Yahoo crumb/session handling.
- **data/analysis/StockEvaluator** – Deterministic rule engine and Mermaid flow generator.
- **ui/** – Compose UI, ViewModel, theming.
- **documentation/** – Product brief, epics, task breakdown.
- **README.md** – You are here.

## Author
Created by **BD** – a retail-investor-friendly tool shared with friends who want guardrails and context before acting on tickers.

## Contributing / Feedback
Issues and improvements are welcome via pull requests. When reporting bugs, include the ticker, device/emulator, and console logs (especially around Yahoo/Groq/Gemini calls) so we can reproduce quickly.

Enjoy the insights and happy investing! 🎯
## Background Sync & Data Sources
- Yahoo Finance remains the primary live quote provider.
- Alpha Vantage acts as a free fallback for PEG, beta, and supplementary fundamentals (set `ALPHA_VANTAGE_API_KEY` in `gradle.properties`).
- WorkManager schedules periodic refreshes that hydrate a Room-backed metrics cache consumed by the Compose UI.
