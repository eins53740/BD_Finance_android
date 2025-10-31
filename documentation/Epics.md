# BD_Finance Android - Implementation Epics
**Version:** 4.0 (Aligned with PRD v4.0)
**Last Updated:** October 2025

These epics provide a structured implementation roadmap for the BD_Finance Android investment advisor app. Each epic represents a major functional area and can be developed iteratively.

---

## Epic 1: Foundation & Data Layer
**Priority:** Critical (MVP Blocker)
**Estimated Duration:** 3-4 weeks

### Objective
Establish the core Android architecture, data layer, and multi-source financial data fetching with intelligent caching.

### Key Deliverables
- Project setup with Hilt dependency injection, Room database, and Retrofit networking
- Multi-provider API clients (Yahoo Finance, Financial Modeling Prep, Alpha Vantage)
- Repository pattern with cache-first strategy and 3-tier fallback logic
- Domain models for stock data, fundamentals, and financial metrics
- TTL-based caching system (10 min for prices, 24h for fundamentals)
- Error handling and graceful degradation for missing/incomplete data

### Success Criteria
- App successfully fetches financial data for any valid US stock ticker
- Cache hit rate > 60% in typical usage scenarios
- Fallback system works: Primary API failure → Secondary → Tertiary → Cached data
- Data freshness indicators accurately reflect cache age

---

## Epic 2: Fundamental Analysis & Scoring Engine
**Priority:** Critical (MVP Blocker)
**Estimated Duration:** 2-3 weeks

### Objective
Implement the core fundamental analysis scoring algorithm that generates BUY/HOLD/SELL recommendations.

### Key Deliverables
- Fundamental metrics calculation engine (P/E, P/B, PEG, ROE, ROA, margins, growth rates, debt ratios, etc.)
- Multi-factor scoring system with weighted categories:
  - Valuation (30%), Profitability (25%), Growth (20%), Financial Health (15%), Dividends (10%)
- Verdict determination logic (score > 0.5 = BUY, 0.0-0.5 = HOLD, < 0.0 = SELL)
- Confidence score calculation based on data quality and metric consistency
- Industry-adjusted thresholds (tech vs utilities vs financials)
- DCF intrinsic valuation calculator

### Success Criteria
- Scoring algorithm produces consistent, explainable verdicts
- Each metric contribution to final score is trackable
- Confidence score accurately reflects data quality issues
- Unit tests cover all scoring logic with edge cases

---

## Epic 3: LLM-Powered Justification System
**Priority:** Critical (MVP Blocker)
**Estimated Duration:** 2 weeks

### Objective
Integrate AI-powered natural language analysis that explains recommendations in investor-friendly language.

### Key Deliverables
- LLM client with dual-provider support (Groq LLaMA 3.1 primary, Gemini fallback)
- Structured prompt generation from computed metrics and verdict
- Response parsing and markdown-to-HTML conversion
- Timeout handling (5s) with cached fallback
- Secure API key storage in BuildConfig and encrypted SharedPreferences
- Token usage tracking and rate limiting
- Fallback to deterministic text if all LLM providers fail

### Success Criteria
- 95%+ of analyses include AI-generated justification
- LLM responses parse correctly and display strengths/concerns/bottom line
- Fallback chain works: Groq → Gemini → Cached → Generic text
- API costs stay under budget with caching and rate limits

---

## Epic 4: Interactive Decision Flowchart Visualizer
**Priority:** High (MVP)
**Estimated Duration:** 2-3 weeks

### Objective
Create visual, interactive flowcharts showing how fundamental metrics flow into the final recommendation.

### Key Deliverables
- Flowchart data model capturing decision tree structure
- Mermaid.js integration via WebView OR custom Compose Canvas drawing
- Dynamic flowchart generation from scoring results
- Interactive features: tap nodes for metric details, color-coded pass/fail indicators
- Animated flow showing decision path in sequence
- Export flowchart as PNG image for sharing
- Responsive layout supporting zoom/pan on small screens

### Success Criteria
- Flowchart accurately represents the decision logic for every stock
- Users can tap any node to see detailed explanations
- Visual styling matches verdict (green for BUY, yellow for HOLD, red for SELL)
- Export feature produces shareable images

---

## Epic 5: Stock Search & Recommendation Display UI
**Priority:** Critical (MVP Blocker)
**Estimated Duration:** 3 weeks

### Objective
Build the core user interface for searching stocks and displaying comprehensive recommendation results.

### Key Deliverables
- Home screen with search bar, recent searches, and quick actions
- Ticker/company name autocomplete and validation
- Voice search integration ("Analyze Tesla stock")
- Loading states with progress indicators
- Recommendation display screen with:
  - Verdict card (BUY/HOLD/SELL badge with confidence)
  - AI justification section (expandable markdown)
  - Key metrics grid with pass/fail indicators
  - Embedded flowchart viewer
  - Action buttons (watchlist, portfolio, alerts, share)
- Error states with retry capability
- Pull-to-refresh functionality
- Material 3 theming (dark/light mode support)

### Success Criteria
- Search responds within 3s (cached) or 8s (fresh)
- UI clearly communicates verdict and reasoning
- All interactive elements are accessible and responsive
- Design follows Material 3 guidelines

---

## Epic 6: Watchlist Management
**Priority:** High (MVP)
**Estimated Duration:** 1-2 weeks

### Objective
Allow users to track multiple stocks and receive alerts when recommendations change.

### Key Deliverables
- Watchlist data model and Room database entities
- Add/remove stocks to watchlist
- Watchlist screen showing ticker, price, change %, recommendation badge
- Background sync using WorkManager (daily refresh)
- Push notifications via Firebase Cloud Messaging when recommendation changes
- Bulk refresh functionality
- Watchlist export to CSV

### Success Criteria
- Users can add unlimited stocks to watchlist
- Background sync uses < 2% battery per day
- Notifications fire within 1 hour of recommendation change
- Watchlist screen shows real-time updates on manual refresh

---

## Epic 7: Portfolio Tracking
**Priority:** High (MVP)
**Estimated Duration:** 2-3 weeks

### Objective
Enable users to track holdings with purchase details and monitor current recommendation vs entry recommendation.

### Key Deliverables
- Portfolio data model (ticker, quantity, purchase price, purchase date)
- Add/edit/delete holdings functionality
- Portfolio screen showing:
  - Total value and gain/loss ($ and %)
  - Individual holding cards with current value vs cost basis
  - Current recommendation badge per holding
  - Best/worst performers
- Portfolio-level metrics:
  - Total return %
  - Sector allocation pie chart
  - Risk score (aggregated from individual stock scores)
- "Suggested Actions" card highlighting holdings with changed recommendations
- Portfolio export to CSV

### Success Criteria
- Accurately calculates gains/losses including dividends (if tracked)
- Portfolio metrics update in real-time
- Users understand which holdings need attention
- Export includes all transaction history

---

## Epic 8: Stock Comparison Tool
**Priority:** Medium (Post-MVP)
**Estimated Duration:** 2 weeks

### Objective
Allow side-by-side comparison of 2-4 stocks to help investors choose between alternatives.

### Key Deliverables
- Multi-stock selection interface
- Comparison screen with:
  - Side-by-side recommendation badges
  - Metric-by-metric comparison table
  - Visual radar charts showing strength profiles
  - AI-generated comparative analysis
  - "Winner" indicator based on score
- Export comparison as PDF or image

### Success Criteria
- Users can compare up to 4 stocks simultaneously
- Comparison clearly highlights differences in key metrics
- AI analysis explains which stock is better for specific investor profiles

---

## Epic 9: Price Alerts & Notifications
**Priority:** Medium (Post-MVP)
**Estimated Duration:** 1-2 weeks

### Objective
Provide customizable alerts for price movements, recommendation changes, and earnings dates.

### Key Deliverables
- Alert configuration UI (target price, % change, recommendation change)
- Background monitoring service via WorkManager
- Push notification system via Firebase Cloud Messaging
- Alert history log
- Snooze/dismiss functionality
- Earnings calendar integration with reminder alerts

### Success Criteria
- Alerts fire within 15 minutes of condition being met
- Users can configure unlimited alerts
- Notifications are actionable (tap to view stock analysis)
- Alert history helps users track decisions

---

## Epic 10: Educational Layer & Tooltips
**Priority:** Medium (Post-MVP)
**Estimated Duration:** 2 weeks

### Objective
Help users understand financial metrics and investment principles through contextual education.

### Key Deliverables
- Tooltip system for all financial metrics
- Interactive glossary of investment terms
- "What is this?" buttons throughout UI
- Onboarding tutorial explaining app features
- Example analyses for demo stocks
- Educational articles/guides on fundamental analysis

### Success Criteria
- Beginners can understand every metric shown
- Tooltips provide clear, concise explanations
- Onboarding completion rate > 70%
- Users report increased financial literacy

---

## Epic 11: Search, Discovery & Screeners
**Priority:** Low (Future Enhancement)
**Estimated Duration:** 3 weeks

### Objective
Enable discovery of investment opportunities through pre-built screens and custom filters.

### Key Deliverables
- Pre-built screeners: "Top BUY recommendations", "Most undervalued", "High growth", "Dividend payers"
- Browse by sector/industry
- Custom screener with multi-criteria filtering
- Saved searches
- Trending stocks discovery
- "Similar stocks" suggestions based on metrics

### Success Criteria
- Screeners surface high-quality investment ideas
- Users discover stocks they wouldn't have found otherwise
- Custom screeners support complex queries
- Performance remains good with large result sets

---

## Epic 12: Data Export & Sharing
**Priority:** Low (Post-MVP)
**Estimated Duration:** 1-2 weeks

### Objective
Allow users to export analyses and share recommendations with others.

### Key Deliverables
- Export stock analysis as PDF report
- Share recommendation as formatted image (social media ready)
- Export portfolio/watchlist to CSV
- Email analysis to self
- Deep linking support (share app links to specific stocks)

### Success Criteria
- Exports are well-formatted and professional
- Shared images are attractive and informative
- Deep links open directly to stock analysis in app

---

## Epic 13: Performance Optimization & Caching
**Priority:** Ongoing (All Phases)
**Estimated Duration:** Continuous

### Objective
Ensure app remains fast, responsive, and efficient as features are added.

### Key Deliverables
- Profile and optimize slow operations
- Implement aggressive preloading for watchlist stocks
- Optimize database queries with proper indexing
- Image caching for company logos
- Background task optimization (WorkManager constraints)
- Memory leak detection and resolution
- APK size optimization

### Success Criteria
- App remains under 50MB download size
- 90% of queries complete within target times
- No memory leaks detected in testing
- Battery usage < 2% per day for typical usage

---

## Epic 14: Testing, Quality & CI/CD
**Priority:** Critical (Ongoing)
**Estimated Duration:** Continuous

### Objective
Maintain high code quality, test coverage, and automated deployment pipeline.

### Key Deliverables
- Unit tests for business logic (target 80% coverage)
- Integration tests for data layer
- UI tests for critical user flows (Compose testing)
- Instrumentation tests for background services
- Firebase Crashlytics integration
- Firebase Analytics event tracking
- CI/CD pipeline with GitHub Actions:
  - Automated testing on PR
  - Lint/static analysis (Detekt, ktlint)
  - Automated APK builds
  - Play Store deployment automation
- Performance monitoring and crash reporting

### Success Criteria
- Test coverage > 80% for business logic
- Crash-free rate > 99.5%
- All PRs require passing tests
- Releases are automated and reliable

---

## Epic Prioritization & Roadmap

### **Phase 1: MVP (Weeks 1-12)**
Must-have features for initial release:
- Epic 1: Foundation & Data Layer
- Epic 2: Fundamental Analysis & Scoring Engine
- Epic 3: LLM-Powered Justification System
- Epic 4: Interactive Decision Flowchart
- Epic 5: Stock Search & Recommendation Display UI
- Epic 6: Watchlist Management
- Epic 7: Portfolio Tracking
- Epic 14: Testing & Quality (baseline)

**MVP Goal:** Users can search any stock, get clear BUY/HOLD/SELL recommendation with AI justification and flowchart, and track stocks/portfolio.

### **Phase 2: Enhancement (Weeks 13-20)**
Value-add features:
- Epic 8: Stock Comparison Tool
- Epic 9: Price Alerts & Notifications
- Epic 10: Educational Layer
- Epic 12: Data Export & Sharing
- Epic 13: Performance Optimization (focused effort)

**Phase 2 Goal:** Improve user engagement, education, and shareability.

### **Phase 3: Growth (Weeks 21+)**
Advanced features:
- Epic 11: Search, Discovery & Screeners
- Additional features from PRD Section 9 (technical analysis, sentiment, etc.)
- Premium feature tier
- International markets expansion

**Phase 3 Goal:** Scale user base and monetization.

