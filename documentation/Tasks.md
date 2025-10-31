# BD_Finance Android - Detailed Task Breakdown
**Version:** 4.0 (Aligned with PRD v4.0 & Epics v4.0)
**Last Updated:** October 2025

This document provides a granular task-level breakdown of each epic. Tasks are organized to support iterative development and can be assigned to individual developers or tackled in sprints.

---

## Epic 1: Foundation & Data Layer

### 1.1 Project Setup & Architecture
- [ ] **1.1.1** Initialize Android project with Kotlin 1.9+, Compose, Material 3
- [ ] **1.1.2** Set up Hilt dependency injection (module structure, application component)
- [ ] **1.1.3** Configure Gradle build with version catalogs or buildSrc
- [ ] **1.1.4** Set up multi-module architecture (app, data, domain, presentation)
- [ ] **1.1.5** Configure ProGuard/R8 rules for release builds
- [ ] **1.1.6** Set up BuildConfig for API keys (GROQ_API_KEY, GEMINI_API_KEY, FMP_API_KEY, etc.)
- [ ] **1.1.7** Configure gradle.properties with placeholder keys for CI/CD

### 1.2 Domain Models
- [ ] **1.2.1** Define StockQuote data class (ticker, price, change, volume, marketCap, etc.)
- [ ] **1.2.2** Define Fundamentals data class (financialMetrics, ratios, growth, margins)
- [ ] **1.2.3** Define FinancialMetrics data class (P/E, P/B, PEG, ROE, ROA, debt/equity, etc.)
- [ ] **1.2.4** Define CompanyInfo data class (name, sector, industry, description, logo URL)
- [ ] **1.2.5** Define HistoricalPrice data class for charting
- [ ] **1.2.6** Define Result sealed class for error handling (Success, Error, Loading)
- [ ] **1.2.7** Define DataQuality data class (missingMetrics count, dataAge, completeness %)

### 1.3 Room Database Setup
- [ ] **1.3.1** Set up Room database with version migration strategy
- [ ] **1.3.2** Create StockQuoteEntity and StockQuoteDao
- [ ] **1.3.3** Create FundamentalsEntity and FundamentalsDao
- [ ] **1.3.4** Create CompanyInfoEntity and CompanyInfoDao
- [ ] **1.3.5** Create AnalysisCacheEntity (stores full analysis with TTL)
- [ ] **1.3.6** Add database indices for ticker symbol lookups
- [ ] **1.3.7** Implement cache eviction strategy (LRU or TTL-based)

### 1.4 API Clients - Yahoo Finance
- [ ] **1.4.1** Create YahooFinanceService interface with Retrofit
- [ ] **1.4.2** Implement quote fetching (real-time price data)
- [ ] **1.4.3** Implement historical price fetching (daily/weekly/monthly)
- [ ] **1.4.4** Implement basic fundamental data fetching (if available)
- [ ] **1.4.5** Add response models and parsing logic
- [ ] **1.4.6** Implement error handling and timeout configuration
- [ ] **1.4.7** Add rate limiting logic (unofficial API may have limits)

### 1.5 API Clients - Financial Modeling Prep
- [ ] **1.5.1** Create FMPService interface with Retrofit
- [ ] **1.5.2** Implement company profile endpoint
- [ ] **1.5.3** Implement financial ratios endpoint (P/E, P/B, ROE, debt, etc.)
- [ ] **1.5.4** Implement income statement endpoint
- [ ] **1.5.5** Implement balance sheet endpoint
- [ ] **1.5.6** Implement cash flow statement endpoint
- [ ] **1.5.7** Implement key metrics endpoint (historical metrics)
- [ ] **1.5.8** Add FMP API key authentication
- [ ] **1.5.9** Handle free tier rate limits (250 calls/day)

### 1.6 API Clients - Alpha Vantage
- [ ] **1.6.1** Create AlphaVantageService interface with Retrofit
- [ ] **1.6.2** Implement quote endpoint (backup for Yahoo)
- [ ] **1.6.3** Implement fundamental data endpoints (company overview, income statement)
- [ ] **1.6.4** Add Alpha Vantage API key authentication
- [ ] **1.6.5** Handle free tier rate limits (25 calls/day - use sparingly)

### 1.7 Repository Layer with Fallback Logic
- [ ] **1.7.1** Create StockDataRepository interface
- [ ] **1.7.2** Implement cache-first strategy: check Room DB before network
- [ ] **1.7.3** Implement 3-tier API fallback: Yahoo → FMP → Alpha Vantage
- [ ] **1.7.4** Add TTL checking logic (10 min for quotes, 24h for fundamentals)
- [ ] **1.7.5** Implement parallel fetching for multiple data sources (Kotlin coroutines)
- [ ] **1.7.6** Handle partial data scenarios (some fields missing from one provider)
- [ ] **1.7.7** Implement data merging logic when combining multiple sources
- [ ] **1.7.8** Add logging for cache hits/misses and API fallback events
- [ ] **1.7.9** Create RepositoryException types for different failure modes

### 1.8 Data Freshness & Quality Tracking
- [ ] **1.8.1** Add timestamp tracking to all cached entities
- [ ] **1.8.2** Implement data quality scoring (completeness, age, consistency)
- [ ] **1.8.3** Create UI indicators for stale data warnings
- [ ] **1.8.4** Add force-refresh capability bypassing cache

### 1.9 Testing
- [ ] **1.9.1** Unit tests for domain models and data transformations
- [ ] **1.9.2** Unit tests for repository fallback logic (mock API clients)
- [ ] **1.9.3** Integration tests for Room database operations
- [ ] **1.9.4** Mock API response tests for each provider
- [ ] **1.9.5** Test TTL expiration logic
- [ ] **1.9.6** Test parallel data fetching concurrency

---

## Epic 2: Fundamental Analysis & Scoring Engine

### 2.1 Metrics Calculation Engine
- [ ] **2.1.1** Create MetricsCalculator class with calculation methods
- [ ] **2.1.2** Implement P/E ratio calculation (handle negative earnings)
- [ ] **2.1.3** Implement P/B ratio calculation
- [ ] **2.1.4** Implement PEG ratio calculation (P/E / earnings growth rate)
- [ ] **2.1.5** Implement Price-to-Sales ratio
- [ ] **2.1.6** Implement ROE calculation (net income / shareholder equity)
- [ ] **2.1.7** Implement ROA calculation (net income / total assets)
- [ ] **2.1.8** Implement ROIC calculation
- [ ] **2.1.9** Implement profit margin calculations (gross, operating, net)
- [ ] **2.1.10** Implement revenue growth rate (YoY, 5-year CAGR)
- [ ] **2.1.11** Implement earnings growth rate (YoY, 5-year CAGR)
- [ ] **2.1.12** Implement debt-to-equity ratio
- [ ] **2.1.13** Implement current ratio and quick ratio
- [ ] **2.1.14** Implement interest coverage ratio (EBIT / interest expense)
- [ ] **2.1.15** Implement free cash flow calculation
- [ ] **2.1.16** Implement dividend yield and payout ratio
- [ ] **2.1.17** Add null/missing data handling for all calculations

### 2.2 Intrinsic Valuation (DCF)
- [ ] **2.2.1** Create DCFCalculator class
- [ ] **2.2.2** Implement free cash flow projection (5-year forecast)
- [ ] **2.2.3** Implement discount rate calculation (WACC or required return)
- [ ] **2.2.4** Implement terminal value calculation (perpetuity growth method)
- [ ] **2.2.5** Calculate present value of cash flows
- [ ] **2.2.6** Calculate intrinsic value per share
- [ ] **2.2.7** Calculate margin of safety (intrinsic value vs current price)

### 2.3 Industry Comparison Data
- [ ] **2.3.1** Define industry/sector taxonomy (GICS codes or similar)
- [ ] **2.3.2** Fetch or hardcode industry average metrics (P/E, ROE, debt/equity)
- [ ] **2.3.3** Implement industry-specific threshold adjustments
- [ ] **2.3.4** Create industry comparison calculations (stock vs industry avg)

### 2.4 Scoring System - Valuation
- [ ] **2.4.1** Create ScoringEngine class with weighted scoring logic
- [ ] **2.4.2** Implement P/E ratio scoring (-1, 0, +1 based on thresholds)
- [ ] **2.4.3** Implement P/B ratio scoring
- [ ] **2.4.4** Implement PEG ratio scoring
- [ ] **2.4.5** Implement DCF valuation scoring (margin of safety)
- [ ] **2.4.6** Aggregate valuation category score (weight: 30%)

### 2.5 Scoring System - Profitability
- [ ] **2.5.1** Implement ROE scoring
- [ ] **2.5.2** Implement ROA scoring
- [ ] **2.5.3** Implement profit margin scoring (with YoY trend)
- [ ] **2.5.4** Implement ROIC scoring
- [ ] **2.5.5** Aggregate profitability category score (weight: 25%)

### 2.6 Scoring System - Growth
- [ ] **2.6.1** Implement revenue growth scoring
- [ ] **2.6.2** Implement earnings growth scoring
- [ ] **2.6.3** Implement forward estimates scoring (if available)
- [ ] **2.6.4** Aggregate growth category score (weight: 20%)

### 2.7 Scoring System - Financial Health
- [ ] **2.7.1** Implement debt-to-equity scoring (sector-adjusted)
- [ ] **2.7.2** Implement current ratio scoring
- [ ] **2.7.3** Implement quick ratio scoring
- [ ] **2.7.4** Implement interest coverage scoring
- [ ] **2.7.5** Implement free cash flow scoring
- [ ] **2.7.6** Aggregate financial health category score (weight: 15%)

### 2.8 Scoring System - Shareholder Returns
- [ ] **2.8.1** Implement dividend yield scoring
- [ ] **2.8.2** Implement payout ratio scoring (sustainability check)
- [ ] **2.8.3** Implement dividend growth rate scoring
- [ ] **2.8.4** Implement share buyback scoring (if data available)
- [ ] **2.8.5** Aggregate shareholder returns score (weight: 10%)

### 2.9 Final Verdict Calculation
- [ ] **2.9.1** Implement weighted aggregation of all category scores
- [ ] **2.9.2** Create Recommendation enum (BUY, HOLD, SELL)
- [ ] **2.9.3** Implement verdict determination (score > 0.5 = BUY, etc.)
- [ ] **2.9.4** Create ScoringResult data class with breakdown by category
- [ ] **2.9.5** Store individual metric contributions for flowchart generation

### 2.10 Confidence Score Calculation
- [ ] **2.10.1** Implement data completeness scoring (penalize missing metrics)
- [ ] **2.10.2** Implement data freshness scoring (penalize stale data)
- [ ] **2.10.3** Implement signal consistency check (penalize conflicting metrics)
- [ ] **2.10.4** Implement liquidity check (penalize low-volume stocks)
- [ ] **2.10.5** Calculate final confidence score (0-100%)

### 2.11 Testing
- [ ] **2.11.1** Unit tests for all metric calculations with edge cases
- [ ] **2.11.2** Unit tests for DCF intrinsic valuation
- [ ] **2.11.3** Unit tests for scoring logic with known stock examples
- [ ] **2.11.4** Test verdict determination for boundary cases (score = 0.5, 0.0, etc.)
- [ ] **2.11.5** Test confidence score calculation with various data quality scenarios
- [ ] **2.11.6** Regression tests with historical stock data to validate scoring consistency

---

## Epic 3: LLM-Powered Justification System

### 3.1 LLM Client - Groq Integration
- [ ] **3.1.1** Create GroqApiService interface with Retrofit
- [ ] **3.1.2** Implement chat completion endpoint (LLaMA 3.1 model)
- [ ] **3.1.3** Add Groq API key authentication (BuildConfig)
- [ ] **3.1.4** Configure timeout (5 seconds)
- [ ] **3.1.5** Handle rate limiting and quota errors
- [ ] **3.1.6** Add retry logic with exponential backoff

### 3.2 LLM Client - Gemini Integration
- [ ] **3.2.1** Create GeminiApiService interface with Retrofit
- [ ] **3.2.2** Implement generateContent endpoint (Gemini 1.5 Pro)
- [ ] **3.2.3** Add Gemini API key authentication
- [ ] **3.2.4** Configure timeout (8 seconds for fallback)
- [ ] **3.2.5** Handle rate limiting and quota errors

### 3.3 Prompt Engineering
- [ ] **3.3.1** Create PromptBuilder class
- [ ] **3.3.2** Define structured prompt template with placeholders
- [ ] **3.3.3** Implement prompt generation from ScoringResult
- [ ] **3.3.4** Include all key metrics with industry context in prompt
- [ ] **3.3.5** Add scoring breakdown to prompt
- [ ] **3.3.6** Specify output format (Strengths/Concerns/Bottom Line structure)
- [ ] **3.3.7** Add constraints (200-300 words, investor-friendly language)
- [ ] **3.3.8** Test prompt with various stock scenarios

### 3.4 Response Parsing
- [ ] **3.4.1** Create LLMResponse data class
- [ ] **3.4.2** Implement markdown parsing to extract sections (Strengths, Concerns, Bottom Line)
- [ ] **3.4.3** Implement markdown-to-HTML conversion for display
- [ ] **3.4.4** Add validation to ensure response contains required sections
- [ ] **3.4.5** Handle malformed or incomplete responses gracefully
- [ ] **3.4.6** Strip any unsafe HTML/scripts for security

### 3.5 LLM Service with Fallback Chain
- [ ] **3.5.1** Create LLMJustificationService class
- [ ] **3.5.2** Implement primary call to Groq with timeout
- [ ] **3.5.3** Implement fallback to Gemini on Groq failure/timeout
- [ ] **3.5.4** Implement cache lookup for previous justifications
- [ ] **3.5.5** Implement deterministic fallback text when all fail
- [ ] **3.5.6** Store successful justifications in cache (AnalysisCacheEntity)
- [ ] **3.5.7** Add provider attribution to response (e.g., "Powered by Groq LLaMA")

### 3.6 Token Usage & Rate Limiting
- [ ] **3.6.1** Track token usage per API call (estimate from response)
- [ ] **3.6.2** Implement daily/monthly token budget tracking
- [ ] **3.6.3** Add throttling when approaching budget limits
- [ ] **3.6.4** Alert user when LLM features are disabled due to quota

### 3.7 API Key Management
- [ ] **3.7.1** Store API keys securely in encrypted SharedPreferences
- [ ] **3.7.2** Obfuscate keys in BuildConfig using base64 or similar
- [ ] **3.7.3** Validate API keys on first use
- [ ] **3.7.4** Provide UI for users to enter their own keys (optional)

### 3.8 Testing
- [ ] **3.8.1** Unit tests for prompt generation with various stock scenarios
- [ ] **3.8.2** Mock tests for Groq API integration
- [ ] **3.8.3** Mock tests for Gemini API integration
- [ ] **3.8.4** Test fallback chain logic (Groq fail → Gemini → Cache → Generic)
- [ ] **3.8.5** Test markdown parsing with various response formats
- [ ] **3.8.6** Test timeout and error handling
- [ ] **3.8.7** Integration test with real API calls (use test budget)

---

## Epic 4: Interactive Decision Flowchart Visualizer

### 4.1 Flowchart Data Model
- [ ] **4.1.1** Create FlowchartNode data class (id, label, type, status, metricValue)
- [ ] **4.1.2** Create FlowchartEdge data class (from, to, label, condition)
- [ ] **4.1.3** Create FlowchartGraph data class (nodes, edges, layout)
- [ ] **4.1.4** Define node types (Start, Decision, Result, Final)
- [ ] **4.1.5** Define node status (Pass, Fail, Neutral, Active)

### 4.2 Flowchart Generation from Scoring
- [ ] **4.2.1** Create FlowchartGenerator class
- [ ] **4.2.2** Convert ScoringResult into flowchart graph structure
- [ ] **4.2.3** Create nodes for each metric evaluation (Valuation, Profitability, Growth, Health)
- [ ] **4.2.4** Create edges showing decision flow
- [ ] **4.2.5** Mark nodes as Pass/Fail based on individual metric scores
- [ ] **4.2.6** Highlight the decision path leading to final verdict
- [ ] **4.2.7** Add metric values and thresholds to node labels

### 4.3 Mermaid.js Integration (Option A)
- [ ] **4.3.1** Generate Mermaid syntax from FlowchartGraph
- [ ] **4.3.2** Create WebView component for rendering Mermaid
- [ ] **4.3.3** Load Mermaid.js library from CDN or local assets
- [ ] **4.3.4** Apply color theming (green/yellow/red based on verdict)
- [ ] **4.3.5** Enable JavaScript interface for node tap events
- [ ] **4.3.6** Handle WebView lifecycle and memory management

### 4.4 Custom Compose Canvas Drawing (Option B - Alternative)
- [ ] **4.4.1** Create custom Compose Canvas-based flowchart renderer
- [ ] **4.4.2** Implement node drawing (rounded rectangles, diamonds, circles)
- [ ] **4.4.3** Implement edge drawing (lines with arrows)
- [ ] **4.4.4** Implement automatic layout algorithm (hierarchical or force-directed)
- [ ] **4.4.5** Add touch detection for node selection
- [ ] **4.4.6** Implement color theming

### 4.5 Interactive Features
- [ ] **4.5.1** Implement node tap/click handling
- [ ] **4.5.2** Show metric detail dialog when node is tapped
- [ ] **4.5.3** Display metric value, threshold, and pass/fail explanation
- [ ] **4.5.4** Implement animation showing decision flow sequence
- [ ] **4.5.5** Add zoom/pan gestures for large flowcharts
- [ ] **4.5.6** Highlight active path with animation

### 4.6 Export Functionality
- [ ] **4.6.1** Implement flowchart to Bitmap conversion
- [ ] **4.6.2** Add "Export as Image" button
- [ ] **4.6.3** Save exported image to device gallery
- [ ] **4.6.4** Add share intent for exported image
- [ ] **4.6.5** Handle storage permissions for Android 10+

### 4.7 UI Integration
- [ ] **4.7.1** Create FlowchartViewer composable
- [ ] **4.7.2** Add preview mode (collapsed view) on recommendation screen
- [ ] **4.7.3** Add fullscreen mode for detailed exploration
- [ ] **4.7.4** Implement smooth transitions between preview and fullscreen
- [ ] **4.7.5** Add loading state while flowchart generates

### 4.8 Testing
- [ ] **4.8.1** Unit tests for flowchart generation logic
- [ ] **4.8.2** Test Mermaid syntax generation (if using Mermaid)
- [ ] **4.8.3** Test custom rendering (if using Canvas)
- [ ] **4.8.4** UI tests for node interaction
- [ ] **4.8.5** Test export functionality
- [ ] **4.8.6** Visual regression tests for consistent rendering

---

## Epic 5: Stock Search & Recommendation Display UI

### 5.1 Navigation Architecture
- [ ] **5.1.1** Set up Jetpack Navigation Compose
- [ ] **5.1.2** Define navigation graph (Home, StockAnalysis, Watchlist, Portfolio, Settings)
- [ ] **5.1.3** Implement bottom navigation bar
- [ ] **5.1.4** Add deep linking support for stock tickers

### 5.2 Home Screen - Search Interface
- [ ] **5.2.1** Create HomeScreen composable
- [ ] **5.2.2** Implement search TextField with Material 3 styling
- [ ] **5.2.3** Add ticker symbol validation (uppercase, 1-5 chars)
- [ ] **5.2.4** Implement autocomplete dropdown (recent + suggested tickers)
- [ ] **5.2.5** Add voice search button and speech recognition integration
- [ ] **5.2.6** Display recent searches as chips (with X to remove)
- [ ] **5.2.7** Add quick action cards ("My Watchlist", "Top Picks", etc.)
- [ ] **5.2.8** Implement empty state with helpful tips

### 5.3 ViewModel - Stock Analysis
- [ ] **5.3.1** Create StockAnalysisViewModel with Hilt injection
- [ ] **5.3.2** Define StockAnalysisUiState sealed class (Loading, Success, Error)
- [ ] **5.3.3** Implement StateFlow for UI state management
- [ ] **5.3.4** Orchestrate data fetching, scoring, LLM, and flowchart generation
- [ ] **5.3.5** Handle errors and expose user-friendly error messages
- [ ] **5.3.6** Add retry mechanism for failed operations
- [ ] **5.3.7** Implement force-refresh bypassing cache

### 5.4 Loading State
- [ ] **5.4.1** Create LoadingScreen composable
- [ ] **5.4.2** Add animated progress indicator
- [ ] **5.4.3** Show progress stages ("Fetching data...", "Analyzing...", "Generating insights...")
- [ ] **5.4.4** Add estimated time remaining indicator
- [ ] **5.4.5** Implement skeleton loading placeholders

### 5.5 Error State
- [ ] **5.5.1** Create ErrorScreen composable
- [ ] **5.5.2** Display user-friendly error messages by error type
- [ ] **5.5.3** Add retry button
- [ ] **5.5.4** Show diagnostic info for debugging (in debug builds)
- [ ] **5.5.5** Handle offline state specifically (cached data available message)

### 5.6 Recommendation Display - Verdict Card
- [ ] **5.6.1** Create VerdictCard composable
- [ ] **5.6.2** Display large BUY/HOLD/SELL badge with color coding
- [ ] **5.6.3** Show confidence score with progress indicator
- [ ] **5.6.4** Display current price, day change, and % change
- [ ] **5.6.5** Show company name, ticker, sector, market cap
- [ ] **5.6.6** Add company logo (fetched via API or placeholder)
- [ ] **5.6.7** Animate card entrance

### 5.7 Recommendation Display - AI Justification
- [ ] **5.7.1** Create AIJustificationCard composable
- [ ] **5.7.2** Render markdown-formatted LLM response as HTML
- [ ] **5.7.3** Implement expand/collapse functionality
- [ ] **5.7.4** Highlight key metrics within text (clickable for details)
- [ ] **5.7.5** Show provider attribution ("Powered by Groq LLaMA 3.1")
- [ ] **5.7.6** Add "last updated" timestamp
- [ ] **5.7.7** Handle missing LLM response (show deterministic fallback)

### 5.8 Recommendation Display - Key Metrics Grid
- [ ] **5.8.1** Create MetricsGrid composable
- [ ] **5.8.2** Display 5-6 key metrics in card layout (P/E, ROE, Growth, Debt, Dividend)
- [ ] **5.8.3** Show metric value, industry comparison, and pass/fail indicator
- [ ] **5.8.4** Color-code based on score (green = good, red = bad)
- [ ] **5.8.5** Make each card tappable to show detailed explanation
- [ ] **5.8.6** Add tooltip/info icon for metric definitions

### 5.9 Recommendation Display - Flowchart Integration
- [ ] **5.9.1** Embed FlowchartViewer in recommendation screen
- [ ] **5.9.2** Show preview mode (collapsed) by default
- [ ] **5.9.3** Add "View Full Flowchart" button to expand
- [ ] **5.9.4** Implement modal or fullscreen flowchart view

### 5.10 Action Buttons
- [ ] **5.10.1** Create action button row composable
- [ ] **5.10.2** Add "Add to Watchlist" button with icon
- [ ] **5.10.3** Add "Add to Portfolio" button (opens entry dialog)
- [ ] **5.10.4** Add "Set Alert" button
- [ ] **5.10.5** Add "Share" button (opens share sheet)
- [ ] **5.10.6** Add "View Full Analysis" button (navigates to detail screen)
- [ ] **5.10.7** Update button states (e.g., "Already in Watchlist")

### 5.11 Pull-to-Refresh
- [ ] **5.11.1** Implement SwipeRefresh wrapper for recommendation screen
- [ ] **5.11.2** Trigger force-refresh on pull gesture
- [ ] **5.11.3** Show refreshing indicator
- [ ] **5.11.4** Update timestamp after refresh

### 5.12 Material 3 Theming
- [ ] **5.12.1** Define app theme with Material 3 color scheme
- [ ] **5.12.2** Implement dynamic color support (Material You on Android 12+)
- [ ] **5.12.3** Create dark and light theme variants
- [ ] **5.12.4** Add theme toggle in settings
- [ ] **5.12.5** Ensure all composables respect theme colors

### 5.13 Accessibility
- [ ] **5.13.1** Add content descriptions to all interactive elements
- [ ] **5.13.2** Ensure proper contrast ratios (WCAG AA)
- [ ] **5.13.3** Test with TalkBack screen reader
- [ ] **5.13.4** Support dynamic text sizing
- [ ] **5.13.5** Add semantic labels for screen readers

### 5.14 Testing
- [ ] **5.14.1** Compose UI tests for HomeScreen
- [ ] **5.14.2** Compose UI tests for recommendation display
- [ ] **5.14.3** Test ViewModel state transitions (Loading → Success → Error)
- [ ] **5.14.4** Test user interactions (button clicks, navigation)
- [ ] **5.14.5** Snapshot tests for visual regression
- [ ] **5.14.6** Accessibility tests

---

## Epic 6: Watchlist Management

### 6.1 Data Model
- [ ] **6.1.1** Create WatchlistItem entity for Room database
- [ ] **6.1.2** Define fields: ticker, addedDate, lastPrice, lastRecommendation
- [ ] **6.1.3** Create WatchlistDao with CRUD operations
- [ ] **6.1.4** Add unique constraint on ticker

### 6.2 Repository
- [ ] **6.2.1** Create WatchlistRepository interface
- [ ] **6.2.2** Implement add/remove/get operations
- [ ] **6.2.3** Expose Flow of watchlist items for reactive UI
- [ ] **6.2.4** Implement bulk operations (remove multiple, export all)

### 6.3 ViewModel
- [ ] **6.3.1** Create WatchlistViewModel
- [ ] **6.3.2** Expose StateFlow of watchlist items
- [ ] **6.3.3** Implement refresh functionality (fetch latest data for all items)
- [ ] **6.3.4** Track refresh status and errors

### 6.4 Watchlist Screen UI
- [ ] **6.4.1** Create WatchlistScreen composable
- [ ] **6.4.2** Display list of watchlist items with LazyColumn
- [ ] **6.4.3** Each row shows: ticker, company name, price, change %, recommendation badge
- [ ] **6.4.4** Color-code rows based on recommendation
- [ ] **6.4.5** Add swipe-to-delete gesture
- [ ] **6.4.6** Implement tap to navigate to stock analysis
- [ ] **6.4.7** Add FAB for bulk actions (export, refresh all)
- [ ] **6.4.8** Show empty state when no items in watchlist

### 6.5 Add to Watchlist Flow
- [ ] **6.5.1** Add "Add to Watchlist" button on stock analysis screen
- [ ] **6.5.2** Show confirmation snackbar on add
- [ ] **6.5.3** Update button state if already in watchlist ("Remove from Watchlist")
- [ ] **6.5.4** Handle errors (duplicate, database error, etc.)

### 6.6 Background Sync with WorkManager
- [ ] **6.6.1** Create WatchlistSyncWorker
- [ ] **6.6.2** Schedule periodic sync (daily at user-configurable time)
- [ ] **6.6.3** Fetch latest data for all watchlist stocks
- [ ] **6.6.4** Compare recommendations with previous state
- [ ] **6.6.5** Trigger notifications for changed recommendations
- [ ] **6.6.6** Add constraints (WiFi only, charging, etc. - configurable)
- [ ] **6.6.7** Handle worker failures and retries

### 6.7 Push Notifications
- [ ] **6.7.1** Set up Firebase Cloud Messaging
- [ ] **6.7.2** Create notification channel for watchlist alerts
- [ ] **6.7.3** Build notification with ticker, old vs new recommendation
- [ ] **6.7.4** Add tap action to open stock analysis
- [ ] **6.7.5** Group notifications if multiple stocks change
- [ ] **6.7.6** Add notification settings (enable/disable, quiet hours)

### 6.8 Export Functionality
- [ ] **6.8.1** Implement CSV export for watchlist
- [ ] **6.8.2** Include ticker, company name, current price, recommendation, date added
- [ ] **6.8.3** Add share intent for exported file
- [ ] **6.8.4** Handle storage permissions

### 6.9 Testing
- [ ] **6.9.1** Unit tests for WatchlistRepository
- [ ] **6.9.2** Test ViewModel state management
- [ ] **6.9.3** UI tests for watchlist screen interactions
- [ ] **6.9.4** Test background worker logic (mock WorkManager)
- [ ] **6.9.5** Test notification delivery

---

## Epic 7: Portfolio Tracking

### 7.1 Data Model
- [ ] **7.1.1** Create PortfolioHolding entity for Room database
- [ ] **7.1.2** Define fields: ticker, quantity, purchasePrice, purchaseDate, notes
- [ ] **7.1.3** Create PortfolioDao with CRUD operations
- [ ] **7.1.4** Add support for multiple purchases of same ticker (lots)

### 7.2 Repository
- [ ] **7.2.1** Create PortfolioRepository interface
- [ ] **7.2.2** Implement add/edit/delete operations
- [ ] **7.2.3** Expose Flow of holdings for reactive UI
- [ ] **7.2.4** Implement portfolio-level aggregation (total value, gain/loss)

### 7.3 Portfolio Calculations
- [ ] **7.3.1** Calculate current value per holding (quantity * current price)
- [ ] **7.3.2** Calculate cost basis (quantity * purchase price)
- [ ] **7.3.3** Calculate gain/loss ($ and %)
- [ ] **7.3.4** Calculate total portfolio value
- [ ] **7.3.5** Calculate total portfolio return
- [ ] **7.3.6** Calculate sector allocation (fetch sector per ticker, aggregate)
- [ ] **7.3.7** Calculate risk score (weighted average of individual stock scores)
- [ ] **7.3.8** Identify best/worst performers

### 7.4 ViewModel
- [ ] **7.4.1** Create PortfolioViewModel
- [ ] **7.4.2** Expose StateFlow of portfolio holdings with computed metrics
- [ ] **7.4.3** Handle refresh (fetch latest prices and recommendations)
- [ ] **7.4.4** Track loading and error states

### 7.5 Portfolio Screen UI
- [ ] **7.5.1** Create PortfolioScreen composable
- [ ] **7.5.2** Display summary card: total value, total gain/loss, return %
- [ ] **7.5.3** Show holdings list with LazyColumn
- [ ] **7.5.4** Each holding shows: ticker, quantity, current value, gain/loss, recommendation badge
- [ ] **7.5.5** Color-code holdings based on gain/loss
- [ ] **7.5.6** Add tap to view detailed analysis
- [ ] **7.5.7** Add edit/delete actions (swipe or context menu)
- [ ] **7.5.8** Show empty state when portfolio is empty

### 7.6 Add Holding Flow
- [ ] **7.6.1** Create AddHoldingDialog composable
- [ ] **7.6.2** Input fields: ticker, quantity, purchase price, purchase date
- [ ] **7.6.3** Validate inputs (positive numbers, valid date)
- [ ] **7.6.4** Fetch current recommendation on add
- [ ] **7.6.5** Save to database and show confirmation
- [ ] **7.6.6** Handle errors

### 7.7 Edit/Delete Holding
- [ ] **7.7.1** Create EditHoldingDialog (reuse AddHoldingDialog)
- [ ] **7.7.2** Pre-fill with existing values
- [ ] **7.7.3** Implement delete confirmation dialog
- [ ] **7.7.4** Update database and refresh UI

### 7.8 Portfolio-Level Insights
- [ ] **7.8.1** Create SectorAllocationChart composable (pie chart)
- [ ] **7.8.2** Display sector breakdown
- [ ] **7.8.3** Create SuggestedActionsCard composable
- [ ] **7.8.4** Highlight holdings with changed recommendations (BUY → SELL, etc.)
- [ ] **7.8.5** Show risk score with visual indicator

### 7.9 Export Functionality
- [ ] **7.9.1** Implement CSV export for portfolio
- [ ] **7.9.2** Include all transaction details, current values, gains/losses
- [ ] **7.9.3** Add share intent

### 7.10 Testing
- [ ] **7.10.1** Unit tests for portfolio calculations
- [ ] **7.10.2** Test PortfolioRepository operations
- [ ] **7.10.3** Test ViewModel state management
- [ ] **7.10.4** UI tests for portfolio screen interactions
- [ ] **7.10.5** Test add/edit/delete flows

---

## Epic 8: Stock Comparison Tool

### 8.1 Multi-Stock Selection
- [ ] **8.1.1** Create ComparisonScreen composable
- [ ] **8.1.2** Add multi-ticker input (support 2-4 tickers)
- [ ] **8.1.3** Validate all tickers before comparison
- [ ] **8.1.4** Fetch data for all selected stocks in parallel

### 8.2 ViewModel
- [ ] **8.2.1** Create ComparisonViewModel
- [ ] **8.2.2** Expose StateFlow with list of stock analyses
- [ ] **8.2.3** Handle loading states for multiple stocks

### 8.3 Comparison UI - Recommendation Badges
- [ ] **8.3.1** Display recommendation badges side-by-side
- [ ] **8.3.2** Show confidence scores
- [ ] **8.3.3** Highlight "winner" (highest score)

### 8.4 Comparison UI - Metric Table
- [ ] **8.4.1** Create metric comparison table
- [ ] **8.4.2** Rows: P/E, ROE, Growth, Debt/Equity, etc.
- [ ] **8.4.3** Columns: one per stock
- [ ] **8.4.4** Color-code cells (green = best, red = worst)
- [ ] **8.4.5** Make scrollable horizontally if needed

### 8.5 Comparison UI - Radar Chart
- [ ] **8.5.1** Create radar chart composable (use charting library or custom Canvas)
- [ ] **8.5.2** Dimensions: Valuation, Profitability, Growth, Health, Dividends
- [ ] **8.5.3** Overlay multiple stocks on same chart with different colors
- [ ] **8.5.4** Add legend

### 8.6 AI Comparative Analysis
- [ ] **8.6.1** Extend LLM prompt to support comparison ("Which is better for...?")
- [ ] **8.6.2** Generate comparative justification
- [ ] **8.6.3** Display in expandable card

### 8.7 Export Comparison
- [ ] **8.7.1** Export comparison as PDF report
- [ ] **8.7.2** Include recommendation badges, table, chart, and AI analysis
- [ ] **8.7.3** Add share functionality

### 8.8 Testing
- [ ] **8.8.1** Test multi-stock data fetching
- [ ] **8.8.2** UI tests for comparison screen
- [ ] **8.8.3** Test export functionality

---

## Epic 9: Price Alerts & Notifications

### 9.1 Data Model
- [ ] **9.1.1** Create PriceAlert entity for Room database
- [ ] **9.1.2** Define fields: ticker, alertType (price, %change, recommendation), threshold, isActive
- [ ] **9.1.3** Create AlertDao

### 9.2 Alert Configuration UI
- [ ] **9.2.1** Create SetAlertDialog composable
- [ ] **9.2.2** Alert type selector (target price, % change, recommendation change)
- [ ] **9.2.3** Input threshold value
- [ ] **9.2.4** Save alert to database

### 9.3 Alert Monitoring Worker
- [ ] **9.3.1** Create AlertMonitorWorker (WorkManager)
- [ ] **9.3.2** Schedule periodic checks (every 30 min or configurable)
- [ ] **9.3.3** Fetch current data for all stocks with active alerts
- [ ] **9.3.4** Check if alert conditions are met
- [ ] **9.3.5** Trigger notification if condition met
- [ ] **9.3.6** Mark alert as triggered or auto-disable

### 9.4 Notifications
- [ ] **9.4.1** Create notification channel for alerts
- [ ] **9.4.2** Build notification with alert details
- [ ] **9.4.3** Add tap action to open stock analysis
- [ ] **9.4.4** Add snooze/dismiss actions

### 9.5 Alert History
- [ ] **9.5.1** Create AlertHistory entity
- [ ] **9.5.2** Log triggered alerts with timestamp
- [ ] **9.5.3** Display alert history in UI

### 9.6 Earnings Calendar Integration
- [ ] **9.6.1** Fetch earnings dates from API (FMP or Alpha Vantage)
- [ ] **9.6.2** Schedule reminders for upcoming earnings
- [ ] **9.6.3** Notify user day before earnings

### 9.7 Testing
- [ ] **9.7.1** Test alert creation and storage
- [ ] **9.7.2** Test alert monitoring logic
- [ ] **9.7.3** Test notification delivery
- [ ] **9.7.4** UI tests for alert configuration

---

## Epic 10: Educational Layer & Tooltips

### 10.1 Tooltip System
- [ ] **10.1.1** Create Tooltip composable with Material styling
- [ ] **10.1.2** Add info icon next to all financial metrics
- [ ] **10.1.3** Define tooltip content for each metric (brief explanation)
- [ ] **10.1.4** Implement show/hide on tap/hover

### 10.2 Glossary
- [ ] **10.2.1** Create Glossary data model (term, definition, example)
- [ ] **10.2.2** Populate glossary with investment terms
- [ ] **10.2.3** Create GlossaryScreen with searchable list
- [ ] **10.2.4** Link from tooltips to glossary details

### 10.3 Onboarding Tutorial
- [ ] **10.3.1** Create onboarding flow (multi-screen)
- [ ] **10.3.2** Explain app features with visuals
- [ ] **10.3.3** Show example stock analysis walkthrough
- [ ] **10.3.4** Add skip button and "Don't show again" option
- [ ] **10.3.5** Show on first launch only

### 10.4 Educational Articles/Guides
- [ ] **10.4.1** Create EducationScreen with article list
- [ ] **10.4.2** Write content: "What is P/E ratio?", "Understanding ROE", etc.
- [ ] **10.4.3** Display articles with markdown rendering
- [ ] **10.4.4** Add bookmark/favorite functionality

### 10.5 Demo Stocks
- [ ] **10.5.1** Preload example analyses for well-known stocks (AAPL, MSFT, etc.)
- [ ] **10.5.2** Add "Try Example" button on home screen
- [ ] **10.5.3** Use cached data for instant demonstration

### 10.6 Testing
- [ ] **10.6.1** Test tooltip display and content
- [ ] **10.6.2** UI tests for glossary and education screens
- [ ] **10.6.3** Test onboarding flow

---

## Epic 11: Search, Discovery & Screeners

### 11.1 Pre-Built Screeners
- [ ] **11.1.1** Create ScreenerScreen composable
- [ ] **11.1.2** Define screener criteria: "Top BUY recommendations", "Most undervalued", "High growth"
- [ ] **11.1.3** Implement screener logic (fetch and filter stocks)
- [ ] **11.1.4** Cache screener results with TTL
- [ ] **11.1.5** Display results in scrollable list

### 11.2 Browse by Sector/Industry
- [ ] **11.2.1** Fetch list of sectors and industries
- [ ] **11.2.2** Create sector/industry browser UI
- [ ] **11.2.3** Show stocks within selected sector

### 11.3 Custom Screener
- [ ] **11.3.1** Create CustomScreenerDialog
- [ ] **11.3.2** Add filters: P/E range, ROE min, growth rate, etc.
- [ ] **11.3.3** Implement multi-criteria filtering logic
- [ ] **11.3.4** Display filtered results

### 11.4 Saved Searches
- [ ] **11.4.1** Allow users to save custom screener configs
- [ ] **11.4.2** Display saved searches with refresh capability

### 11.5 Trending Stocks
- [ ] **11.5.1** Identify trending stocks (most searched, most volatile, etc.)
- [ ] **11.5.2** Display trending list on home screen

### 11.6 Similar Stocks
- [ ] **11.6.1** Implement similarity algorithm (same sector, similar metrics)
- [ ] **11.6.2** Show "Similar Stocks" section on stock analysis screen

### 11.7 Testing
- [ ] **11.7.1** Test screener logic with various criteria
- [ ] **11.7.2** UI tests for screener screens
- [ ] **11.7.3** Test performance with large datasets

---

## Epic 12: Data Export & Sharing

### 12.1 PDF Export
- [ ] **12.1.1** Integrate PDF generation library (e.g., iText, PdfDocument)
- [ ] **12.1.2** Design PDF report template (logo, verdict, metrics, AI analysis)
- [ ] **12.1.3** Implement PDF generation from stock analysis
- [ ] **12.1.4** Save PDF to device and share via intent

### 12.2 Image Sharing
- [ ] **12.2.1** Design shareable image template (social media format)
- [ ] **12.2.2** Render verdict, key metrics, and logo into bitmap
- [ ] **12.2.3** Add share button for image export
- [ ] **12.2.4** Handle storage and share intent

### 12.3 CSV Export
- [ ] **12.3.1** Implement CSV generation for portfolio and watchlist (already covered in Epics 6 & 7)
- [ ] **12.3.2** Add CSV export for full stock analysis (all metrics)

### 12.4 Email Integration
- [ ] **12.4.1** Add "Email to myself" button
- [ ] **12.4.2** Pre-fill email with analysis summary and attachment
- [ ] **12.4.3** Launch email intent

### 12.5 Deep Linking
- [ ] **12.5.1** Configure deep link scheme (bdfinance://stock/{ticker})
- [ ] **12.5.2** Handle deep link navigation to stock analysis
- [ ] **12.5.3** Generate shareable links for stocks
- [ ] **12.5.4** Test deep links from various sources (web, SMS, etc.)

### 12.6 Testing
- [ ] **12.6.1** Test PDF generation and content
- [ ] **12.6.2** Test image export and sharing
- [ ] **12.6.3** Test deep linking

---

## Epic 13: Performance Optimization & Caching

### 13.1 Profiling
- [ ] **13.1.1** Set up Android Profiler for CPU, memory, network analysis
- [ ] **13.1.2** Profile app during typical usage scenarios
- [ ] **13.1.3** Identify slow operations and memory leaks
- [ ] **13.1.4** Create performance benchmarks

### 13.2 Preloading & Prefetching
- [ ] **13.2.1** Implement watchlist preloading on app launch
- [ ] **13.2.2** Prefetch data for recently viewed stocks
- [ ] **13.2.3** Use background threads for non-critical preloading

### 13.3 Database Optimization
- [ ] **13.3.1** Add indices to frequently queried fields (ticker, date)
- [ ] **13.3.2** Optimize complex queries
- [ ] **13.3.3** Implement pagination for large lists

### 13.4 Image Caching
- [ ] **13.4.1** Integrate Coil for efficient image loading
- [ ] **13.4.2** Cache company logos with LRU policy
- [ ] **13.4.3** Use WebP format for smaller image sizes

### 13.5 Background Task Optimization
- [ ] **13.5.1** Optimize WorkManager constraints (WiFi, charging, battery)
- [ ] **13.5.2** Use exponential backoff for retries
- [ ] **13.5.3** Batch background operations when possible

### 13.6 Memory Management
- [ ] **13.6.1** Use LeakCanary to detect memory leaks
- [ ] **13.6.2** Fix identified leaks (ViewModel, coroutine, listener leaks)
- [ ] **13.6.3** Optimize bitmap handling (recycle, scale down)

### 13.7 APK Size Optimization
- [ ] **13.7.1** Enable code shrinking (R8/ProGuard)
- [ ] **13.7.2** Remove unused resources
- [ ] **13.7.3** Use vector drawables instead of PNGs
- [ ] **13.7.4** Enable App Bundle for split APKs by ABI/density

### 13.8 Testing
- [ ] **13.8.1** Benchmark tests for critical operations
- [ ] **13.8.2** Memory leak detection tests
- [ ] **13.8.3** Battery usage profiling

---

## Epic 14: Testing, Quality & CI/CD

### 14.1 Unit Tests
- [ ] **14.1.1** Set up JUnit 5 and Mockk
- [ ] **14.1.2** Write unit tests for all business logic (scoring, calculations, etc.)
- [ ] **14.1.3** Target 80%+ code coverage for domain and data layers
- [ ] **14.1.4** Test edge cases and error conditions

### 14.2 Integration Tests
- [ ] **14.2.1** Test data layer with real Room database (in-memory)
- [ ] **14.2.2** Test repository with mock API responses
- [ ] **14.2.3** Test ViewModel with real dependencies (using fakes)

### 14.3 UI Tests
- [ ] **14.3.1** Set up Compose testing framework
- [ ] **14.3.2** Write UI tests for all screens (search, analysis, watchlist, portfolio)
- [ ] **14.3.3** Test user flows (search → analyze → add to watchlist)
- [ ] **14.3.4** Test accessibility with TalkBack

### 14.4 Instrumentation Tests
- [ ] **14.4.1** Test background workers (WorkManager testing library)
- [ ] **14.4.2** Test notifications
- [ ] **14.4.3** Test database migrations

### 14.5 Crashlytics Integration
- [ ] **14.5.1** Set up Firebase Crashlytics
- [ ] **14.5.2** Add crash reporting to catch blocks
- [ ] **14.5.3** Add custom keys for debugging context
- [ ] **14.5.4** Monitor crash-free rate on dashboard

### 14.6 Analytics Integration
- [ ] **14.6.1** Set up Firebase Analytics
- [ ] **14.6.2** Track key events: stock_search, analysis_viewed, watchlist_add, etc.
- [ ] **14.6.3** Track user properties: premium_user, watchlist_count
- [ ] **14.6.4** Monitor engagement metrics on dashboard

### 14.7 CI/CD Pipeline
- [ ] **14.7.1** Set up GitHub Actions workflow
- [ ] **14.7.2** Run unit tests on every PR
- [ ] **14.7.3** Run lint and static analysis (Detekt, ktlint)
- [ ] **14.7.4** Build debug APK on PR
- [ ] **14.7.5** Build release APK on merge to main
- [ ] **14.7.6** Sign APK with release keystore
- [ ] **14.7.7** Deploy to Google Play Internal Testing track (manual approval)

### 14.8 Code Quality Tools
- [ ] **14.8.1** Configure Detekt for Kotlin static analysis
- [ ] **14.8.2** Configure ktlint for code formatting
- [ ] **14.8.3** Add pre-commit hooks for formatting
- [ ] **14.8.4** Set up code coverage reporting (JaCoCo)

### 14.9 Play Store Deployment
- [ ] **14.9.1** Create Play Store listing (description, screenshots, icon)
- [ ] **14.9.2** Set up release tracks (internal, alpha, beta, production)
- [ ] **14.9.3** Automate deployment with fastlane or GitHub Actions
- [ ] **14.9.4** Implement staged rollout strategy

### 14.10 Testing
- [ ] **14.10.1** Verify all tests run in CI pipeline
- [ ] **14.10.2** Test deployment process end-to-end
- [ ] **14.10.3** Verify crash reporting and analytics

---

## Task Prioritization Summary

### **Sprint 1-2: Foundation (Weeks 1-4)**
- Epic 1 (Foundation & Data Layer) - all tasks

### **Sprint 3-4: Core Analysis (Weeks 5-8)**
- Epic 2 (Scoring Engine) - all tasks
- Epic 3 (LLM Justification) - all tasks

### **Sprint 5-6: Visualization & UI (Weeks 9-12)**
- Epic 4 (Flowchart) - all tasks
- Epic 5 (Stock Search & Display) - all tasks

### **Sprint 7-8: User Features (Weeks 13-16)**
- Epic 6 (Watchlist) - all tasks
- Epic 7 (Portfolio) - all tasks

### **Sprint 9-10: Enhancement (Weeks 17-20)**
- Epic 8, 9, 10, 12 as capacity allows

### **Ongoing Throughout**
- Epic 14 (Testing & CI/CD) - continuous integration
- Epic 13 (Performance) - ongoing optimization

---

**Total Estimated Tasks:** 350+
**Estimated MVP Completion:** 12 weeks with 2-3 developers
**Full Feature Set:** 20+ weeks
