# BD Finance Android - Quick Test Checklist ✓

## Setup (One-time)
```bash
☐ Install Java JDK 17 or 21
☐ Verify: java -version works
☐ Build app: gradlew.bat :app:assembleDebug
☐ Install: gradlew.bat :app:installDebug
```

## 🏠 Home Tab - Stock Analysis
```
☐ Enter "AAPL" → Analyze → Shows results
☐ Verdict badge displays (BUY/CAUTION/DO NOT BUY)
☐ Price and % change show
☐ Key metrics visible
☐ "Watchlist" button present
☐ "Portfolio" button present
☐ Refresh button works
☐ Invalid ticker shows error
```

## ⭐ Watchlist Tab
```
☐ Empty state shows on first launch
☐ Add AAPL from Home → appears in Watchlist
☐ Card shows: ticker, name, price, %, badge
☐ Pull-to-refresh works
☐ Tap item → navigates to analysis
☐ Delete button removes item
☐ Add multiple stocks (AAPL, MSFT, GOOGL)
☐ All persist after app restart
```

## 💼 Portfolio Tab
```
☐ Empty state shows with FAB
☐ FAB (+) button visible
☐ Add via analysis → "Portfolio" button → dialog
☐ Dialog pre-fills ticker, name, price
☐ Enter quantity: 10 → Add → success
☐ Summary card shows:
  ☐ Total value
  ☐ Gain/Loss ($ and %)
  ☐ Number of stocks
☐ Holding card shows:
  ☐ Ticker and quantity
  ☐ Current value
  ☐ Gain/Loss
  ☐ Recommendation badge
☐ Delete holding works
☐ Pull-to-refresh updates prices
☐ All persists after app restart
```

## 🔄 Navigation
```
☐ Bottom nav switches tabs
☐ Selected tab is highlighted
☐ State preserved when switching
☐ No crashes during rapid switching
☐ Back button works correctly
```

## 💾 Data Persistence
```
☐ Add stocks to watchlist → close app → reopen → still there
☐ Add holdings to portfolio → close app → reopen → still there
☐ Calculations remain correct after restart
☐ Recommendations persist
```

## ⚡ Performance
```
☐ App launches in < 2 seconds
☐ Tab switching is smooth
☐ Scrolling is smooth
☐ No UI freezes
☐ Network loading shows spinner
```

## 🎨 UI/UX
```
☐ Material 3 design looks good
☐ Light mode works
☐ Dark mode works
☐ Colors are readable
☐ Buttons are tappable
☐ Text is readable
☐ Icons are clear
```

## ❌ Error Handling
```
☐ Network off → shows error message
☐ Invalid ticker → shows error
☐ Empty input → shows validation
☐ Duplicate add → handles gracefully
☐ No crashes with bad input
```

## 📊 Calculations (Portfolio)
```
Test Case 1:
☐ Buy 10 @ $100 → Price $150
☐ Gain = $500 (50%)
☐ Current Value = $1,500
☐ Cost Basis = $1,000

Test Case 2:
☐ Buy 5 @ $200 → Price $180
☐ Loss = -$100 (-10%)
☐ Current Value = $900
☐ Cost Basis = $1,000

Test Case 3:
☐ Multiple holdings
☐ Total value = sum of all
☐ Total gain/loss correct
☐ Percentages accurate
```

## 🔔 Background Sync
```
☐ Check logs for WatchlistSyncWorker
☐ Wait 6+ hours → watchlist auto-updates
☐ Network off → sync fails gracefully
☐ Network on → sync resumes
```

## 📱 Device Testing
```
Device: __________________
Android Version: _________
Build: app-debug.apk

☐ Phone (Portrait)
☐ Phone (Landscape)
☐ Tablet (if available)
☐ Different screen sizes
☐ Different Android versions
```

## 🐛 Bugs Found
```
1. ___________________________
2. ___________________________
3. ___________________________
```

## ✅ Sign-Off
```
Tested By: __________________
Date: ______________________
Status: ☐ Pass  ☐ Fail
```

---

**Pass Criteria**: All core features work without crashes
- Stock analysis works
- Can add to watchlist
- Can add to portfolio
- Data persists
- Navigation works
- No critical bugs

**Ready for Production**: ☐ Yes  ☐ No
