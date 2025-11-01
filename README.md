# 📱 BD Finance - Smart Stock Analysis for Android

> **Your Personal Stock Advisor** - Get clear, explainable Buy/Sell/Hold recommendations with comprehensive analysis, all in your pocket.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-33%2B-brightgreen.svg)](https://android-arsenal.com/api?level=33)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5+-orange.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🌟 What is BD Finance?

BD Finance is a **modern Android app** that turns any stock ticker into a transparent, data-driven investment recommendation. Whether you're a seasoned investor or just starting out, BD Finance gives you:

✅ **Clear Recommendations** - Simple Buy/Caution/Don't Buy verdicts backed by data
✅ **Complete Transparency** - See exactly why each recommendation is made
✅ **Fundamental Analysis** - Comprehensive scoring across valuation, profitability, growth, and health
✅ **Watchlist Tracking** - Monitor your favorite stocks with auto-updates
✅ **Portfolio Management** - Track your investments and see gains/losses in real-time
✅ **AI-Powered Insights** - Optional AI commentary from Groq/Gemini

Built with **Material 3** design and powered by **Jetpack Compose**, BD Finance offers a beautiful, fast, and reliable experience.

---

## 📸 Screenshots

```
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│   🏠 HOME           │  │   ⭐ WATCHLIST      │  │   💼 PORTFOLIO      │
│                     │  │                     │  │                     │
│ [Enter Ticker]      │  │ AAPL                │  │ Total: $12,450.00   │
│ [Analyze Button]    │  │ Apple Inc.          │  │ Gain: +$2,450 (24%) │
│                     │  │ $178.50   +2.3%     │  │ 3 stocks            │
│ ┌─────────────────┐ │  │ [BUY]          [🗑️] │  │                     │
│ │ AAPL            │ │  │                     │  │ ┌─────────────────┐ │
│ │ Apple Inc.      │ │  │ MSFT                │  │ │ AAPL            │ │
│ │ BUY             │ │  │ Microsoft Corp.     │  │ │ 10 @ $150       │ │
│ │ $178.50 (+2.3%) │ │  │ $378.91   +1.5%     │  │ │ Now: $1,785     │ │
│ │                 │ │  │ [BUY]          [🗑️] │  │ │ +$285 (19%) ↗️  │ │
│ │ [Watchlist]     │ │  │                     │  │ └─────────────────┘ │
│ │ [Portfolio]     │ │  │ Pull to refresh...  │  │ [+ FAB]             │
│ └─────────────────┘ │  │                     │  │                     │
└─────────────────────┘  └─────────────────────┘  └─────────────────────┘
```

---

## 🎯 Key Features

### 📊 Stock Analysis
- **Real-time Data** - Live quotes from Yahoo Finance
- **Fundamental Scoring** - Analyzes valuation, profitability, growth, and financial health
- **Intrinsic Valuation** - DCF, Ben Graham, and DDM models
- **Sector Comparison** - See how stocks compare to industry peers
- **Decision Flow** - Visual diagram showing the complete reasoning
- **Risk Assessment** - Beta analysis and volatility metrics
- **Dividend Analysis** - Yield, payout ratio, and sustainability

### ⭐ Watchlist
- **Track Favorites** - Add stocks to monitor with one tap
- **Auto-Updates** - Prices and recommendations refresh every 6 hours
- **Quick Overview** - See all tracked stocks at a glance
- **Pull-to-Refresh** - Manually update prices anytime
- **Smart Notifications** - Get alerted when recommendations change (coming soon)

### 💼 Portfolio
- **Track Investments** - Record your holdings with purchase details
- **Real-Time Gains** - See profit/loss updated with live prices
- **Performance Summary** - Total value, cost basis, and returns
- **Multiple Holdings** - Track as many stocks as you want
- **Detailed Metrics** - Per-holding gains and percentages
- **Easy Entry** - Add holdings from analysis or manually

### 🤖 AI Insights (Optional)
- **Groq LLaMA 3.1** - Fast AI commentary on your stock picks
- **Google Gemini** - Fallback AI for comprehensive analysis
- **Graceful Fallback** - Works without AI if keys aren't provided

### 🎨 Beautiful Design
- **Material 3** - Modern, clean interface
- **Dark Mode** - Easy on the eyes, day or night
- **Smooth Animations** - Polished transitions and interactions
- **Responsive** - Works great on phones and tablets

---

## 🚀 Quick Start

### For Users (Non-Developers)

#### Option 1: Download APK (Easiest)
1. Download the latest APK from [Releases](releases) (when available)
2. On your Android device, enable "Install from Unknown Sources"
3. Open the downloaded APK and install
4. Launch BD Finance from your app drawer

#### Option 2: Install from Android Studio
1. **Install Android Studio** - [Download here](https://developer.android.com/studio)
2. **Clone the project**:
   ```bash
   git clone https://github.com/yourusername/BD_Finance_android.git
   cd BD_Finance_android
   ```
3. **Open in Android Studio** - File → Open → Select project folder
4. **Run** - Click the green ▶️ play button
5. Select your device or emulator

### For Developers

#### Prerequisites
- **JDK 17 or 21** - [Download here](https://adoptium.net/)
- **Android Studio** Hedgehog (2023.1.1) or newer
- **Android SDK** with API 33+
- **Git**

#### Setup Steps
```bash
# 1. Clone the repository
git clone https://github.com/yourusername/BD_Finance_android.git
cd BD_Finance_android

# 2. (Optional) Set up API keys - Create .env file:
echo 'GROQ_API_KEY=your_groq_key_here' > .env
echo 'GEMINI_API_KEY=your_gemini_key_here' >> .env
echo 'FMP_API_KEY=your_fmp_key_here' >> .env
echo 'ALPHA_VANTAGE_API_KEY=your_av_key_here' >> .env

# 3. Build the project
./gradlew clean
./gradlew :app:assembleDebug

# 4. Install on connected device
./gradlew :app:installDebug

# 5. Run tests
./gradlew :app:test
```

#### Running in Android Studio
1. Open the project in Android Studio
2. Sync Gradle (File → Sync Project with Gradle Files)
3. Select device/emulator from dropdown
4. Click Run (▶️) or press Shift+F10

---

## 📖 How to Use

### Analyzing a Stock
1. Open the app → **Home** tab
2. Enter a stock ticker (e.g., "AAPL")
3. Tap **Analyze**
4. View the recommendation and detailed analysis
5. Scroll down to see metrics, fundamentals, and AI insights

### Adding to Watchlist
1. After analyzing a stock, tap **Watchlist** button
2. Stock is added with current price and recommendation
3. Switch to **Watchlist** tab to view all tracked stocks
4. Pull down to refresh prices
5. Swipe or tap 🗑️ to remove stocks

### Building Your Portfolio
1. After analyzing a stock, tap **Portfolio** button
2. Enter:
   - **Quantity** - Number of shares owned
   - **Purchase Price** - Price you paid per share
   - **Purchase Date** - When you bought
   - **Notes** (optional) - Any reminders
3. Tap **Add**
4. View in **Portfolio** tab
5. See real-time gains/losses as prices change

### Understanding Recommendations

**🟢 BUY**
- Strong fundamentals across all metrics
- Good value relative to sector
- Low risk profile
- Sustainable growth

**🟡 BUY WITH CAUTION**
- Mixed signals in metrics
- Some concerns flagged
- Moderate risk
- Requires monitoring

**🔴 DO NOT BUY**
- Multiple red flags
- Overvalued or high risk
- Poor fundamentals
- Not recommended

---

## 🔧 Configuration

### API Keys (Optional)

BD Finance works **without API keys** using cached data and deterministic analysis. However, for enhanced features, you can add:

**Groq API** (AI Commentary)
- Sign up at [groq.com](https://groq.com/)
- Free tier available
- Fast AI insights

**Google Gemini** (AI Commentary Fallback)
- Get key at [ai.google.dev](https://ai.google.dev/)
- Free tier available
- Comprehensive analysis

**Financial Modeling Prep** (Enhanced Fundamentals)
- Sign up at [financialmodelingprep.com](https://financialmodelingprep.com/)
- Free tier: 250 calls/day
- Sector medians and 5Y/10Y data

**Alpha Vantage** (Additional Metrics)
- Get key at [alphavantage.co](https://www.alphavantage.co/)
- Free tier: 25 calls/day
- PEG ratio, beta, supplementary data

#### Adding Keys

**Method 1: .env file (Recommended)**
Create a `.env` file in project root:
```
GROQ_API_KEY=sk-proj-xxxx
GEMINI_API_KEY=AIzaxxxx
FMP_API_KEY=fnm_xxxx
ALPHA_VANTAGE_API_KEY=xxxx
```

**Method 2: gradle.properties**
Add to `~/.gradle/gradle.properties` or project root:
```properties
GROQ_API_KEY=your_key_here
GEMINI_API_KEY=your_key_here
FMP_API_KEY=your_key_here
ALPHA_VANTAGE_API_KEY=your_key_here
```

**Method 3: Environment Variables**
```bash
export GROQ_API_KEY=your_key_here
export GEMINI_API_KEY=your_key_here
```

### Background Sync

Watchlist auto-updates every **6 hours** by default. To customize:

Edit `WatchlistSyncScheduler.kt`:
```kotlin
val syncRequest = PeriodicWorkRequestBuilder<WatchlistSyncWorker>(
    repeatInterval = 6, // Change this number
    repeatIntervalTimeUnit = TimeUnit.HOURS,
    // ...
)
```

---

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin 1.9+
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Networking**: OkHttp + Retrofit
- **Async**: Kotlin Coroutines + Flow
- **Background**: WorkManager
- **DI**: Manual (Factory Pattern)

### Project Structure
```
app/src/main/java/com/example/bd_finance/
├── BDFinanceApplication.kt       # App initialization
├── MainActivity.kt                # Entry point
├── data/
│   ├── BDFinanceDatabase.kt      # Room database
│   ├── StockAnalysisRepository.kt # Main repository
│   ├── analysis/
│   │   └── StockEvaluator.kt     # Evaluation engine
│   ├── fundamentals/
│   │   └── FundamentalAnalysisEngine.kt # Scoring
│   ├── llm/
│   │   └── LargeLanguageModelClient.kt  # AI integration
│   ├── network/
│   │   └── YahooFinanceClient.kt # Quote fetching
│   ├── watchlist/                # Watchlist feature
│   ├── portfolio/                # Portfolio feature
│   └── sync/                     # Background sync
└── ui/
    ├── BDFinanceNavigation.kt    # Navigation setup
    ├── StockEvaluatorScreen.kt   # Main analysis UI
    ├── watchlist/                # Watchlist screens
    ├── portfolio/                # Portfolio screens
    └── theme/                    # Material 3 theme
```

---

## 🧪 Testing

### Running Tests
```bash
# Unit tests
./gradlew :app:test

# UI tests (requires emulator/device)
./gradlew :app:connectedAndroidTest

# Generate coverage report
./gradlew :app:jacocoTestReport
```

### Test Documentation
- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Comprehensive test plan (300+ test cases)
- **[QUICK_TEST_CHECKLIST.md](QUICK_TEST_CHECKLIST.md)** - 5-minute smoke tests
- **[SETUP_AND_RUN.md](SETUP_AND_RUN.md)** - Build and run instructions

---

## 🤝 Contributing

We welcome contributions! Here's how to help:

### Reporting Bugs
1. Check [Issues](issues) to see if already reported
2. Create a new issue with:
   - Clear title
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable
   - Device/Android version

### Suggesting Features
1. Open an issue with `[Feature Request]` in title
2. Describe the feature and use case
3. Explain why it would be valuable

### Submitting Code
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Write/update tests
5. Commit with clear messages (`git commit -m 'Add amazing feature'`)
6. Push to branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Use meaningful variable names
- Write tests for new features
- Update documentation
- Keep PRs focused and small

---

## 📚 Documentation

### For Users
- **[README.md](README.md)** - This file (start here!)
- **[SETUP_AND_RUN.md](SETUP_AND_RUN.md)** - Installation guide
- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - How to test the app

### For Developers
- **[CLAUDE.md](CLAUDE.md)** - Development guide for AI assistants
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Overview of implementation
- **[FINAL_IMPLEMENTATION_REPORT.md](FINAL_IMPLEMENTATION_REPORT.md)** - Complete technical report

### For Product Managers
- **[documentation/BD_Finance_Financial_Advisor_PRD.md](documentation/BD_Finance_Financial_Advisor_PRD.md)** - Product requirements
- **[documentation/Epics.md](documentation/Epics.md)** - Feature epics
- **[documentation/Tasks.md](documentation/Tasks.md)** - Task breakdown

---

## 🔐 Privacy & Security

### Data Collection
- **No personal data collected**
- **No user tracking**
- **No analytics by default**
- Stock symbols and analysis stay on your device

### API Keys
- Stored securely in app
- Never transmitted to third parties (only to respective API providers)
- Can be removed anytime

### Permissions
- **Internet** - Required for fetching stock data
- **Wake Lock** - For background sync (optional)
- No camera, location, or contacts access

---

## 🐛 Troubleshooting

### App Won't Build
**Error**: `JAVA_HOME is not set`
**Solution**: Install JDK 17+ and set JAVA_HOME environment variable

**Error**: `SDK location not found`
**Solution**: Install Android Studio or set ANDROID_HOME

### App Crashes on Launch
**Solution**:
1. Check Logcat: `adb logcat *:E`
2. Uninstall and reinstall
3. Clear app data: Settings → Apps → BD Finance → Storage → Clear Data

### Network Errors
**Error**: "Unable to fetch stock data"
**Solution**:
1. Check internet connection
2. Try different stock ticker
3. Yahoo Finance might be rate limiting - wait a few minutes

### Stock Not Found
**Error**: "Ticker not found"
**Solution**:
1. Verify ticker symbol is correct
2. Use US stock tickers (AAPL, MSFT, etc.)
3. Some foreign stocks may not be available

### Background Sync Not Working
**Solution**:
1. Check battery optimization settings
2. Ensure app has background data access
3. Check network connectivity
4. View logs: `adb logcat -s WatchlistSyncWorker`

---

## 📊 Roadmap

### ✅ Completed (v1.0)
- Stock analysis with recommendations
- Fundamental scoring and valuation
- Watchlist with auto-updates
- Portfolio tracking with gains/losses
- Material 3 UI with dark mode
- Background sync

### 🚧 In Progress (v1.1)
- Push notifications for watchlist changes
- Price alerts
- Export watchlist/portfolio to CSV
- Edit portfolio holdings

### 🔮 Planned (v2.0)
- Stock comparison tool
- Custom screeners
- Sector allocation charts
- Educational tooltips
- Dividend tracker
- Tax lot management
- Performance charts

---

## 💖 Acknowledgments

### Built With
- [Kotlin](https://kotlinlang.org/) - Programming language
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Material 3](https://m3.material.io/) - Design system
- [Room](https://developer.android.com/training/data-storage/room) - Database
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - Background tasks
- [OkHttp](https://square.github.io/okhttp/) - HTTP client

### Data Sources
- [Yahoo Finance](https://finance.yahoo.com/) - Primary quote provider
- [Financial Modeling Prep](https://financialmodelingprep.com/) - Fundamental data
- [Alpha Vantage](https://www.alphavantage.co/) - Additional metrics

### AI Providers
- [Groq](https://groq.com/) - Fast AI inference
- [Google Gemini](https://ai.google.dev/) - AI analysis

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📧 Contact & Support

### Questions?
- Open an [Issue](issues)
- Email: [your-email@example.com]

### Follow Development
- GitHub: [github.com/yourusername/BD_Finance_android](https://github.com/yourusername/BD_Finance_android)

### Disclaimer
⚠️ **Important**: BD Finance is a tool for **educational and informational purposes only**. It is **not financial advice**. Always do your own research and consult with a qualified financial advisor before making investment decisions. Past performance does not guarantee future results. Invest at your own risk.

---

## 🌟 Star History

If you find this project useful, please consider giving it a ⭐ on GitHub!

---

<div align="center">

**Made with ❤️ for retail investors**

*Transparent • Fast • Reliable*

[⬆ Back to Top](#-bd-finance---smart-stock-analysis-for-android)

</div>
