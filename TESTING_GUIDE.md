# BD Finance Android - Testing Guide

## Prerequisites for Running the App

### Required Software
1. **Java Development Kit (JDK)**
   - Download JDK 17 or 21 from: https://adoptium.net/
   - Install and set JAVA_HOME environment variable
   - Verify: `java -version` should work

2. **Android Studio** (Recommended)
   - Download from: https://developer.android.com/studio
   - Includes Android SDK and emulator
   - Easier for testing and debugging

3. **OR Android Command Line Tools**
   - For building from command line only
   - Requires manual SDK setup

### Setting Up Java (Windows)

```powershell
# After installing JDK, set environment variable
# In PowerShell (Admin):
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-17.0.8.101-hotspot', 'Machine')

# Add to PATH
$path = [System.Environment]::GetEnvironmentVariable('Path', 'Machine')
[System.Environment]::SetEnvironmentVariable('Path', "$path;%JAVA_HOME%\bin", 'Machine')

# Restart terminal and verify
java -version
```

## Building the App

### Method 1: Android Studio (Recommended)

1. **Open Project**
   ```
   Open Android Studio → File → Open → Select BD_Finance_android folder
   ```

2. **Sync Gradle**
   - Wait for "Sync Project with Gradle Files" to complete
   - Resolve any dependency issues
   - Should see "BUILD SUCCESSFUL" in Build tab

3. **Run on Emulator**
   - Tools → Device Manager → Create Device
   - Select device (e.g., Pixel 6)
   - Select system image (API 33 or higher)
   - Click Run (green play button) or Shift+F10

4. **Run on Physical Device**
   - Enable Developer Options on device
   - Enable USB Debugging
   - Connect device via USB
   - Select device from dropdown
   - Click Run

### Method 2: Command Line

```bash
# Navigate to project
cd "C:\Github\BD\BD_Finance_android"

# Build debug APK
./gradlew.bat :app:assembleDebug

# Output location
# app/build/outputs/apk/debug/app-debug.apk

# Install to connected device
./gradlew.bat :app:installDebug

# Or install manually
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Comprehensive Test Plan

### Phase 1: Build & Installation Tests

#### 1.1 Compilation
- [ ] Project builds without errors
- [ ] No Gradle dependency conflicts
- [ ] Room schema generates correctly
- [ ] R8/ProGuard rules don't break code

#### 1.2 Installation
- [ ] APK installs on Android 13 device
- [ ] APK installs on Android 14 device
- [ ] App icon appears in launcher
- [ ] App name displays correctly

### Phase 2: Navigation Tests

#### 2.1 Bottom Navigation
- [ ] **Test**: Launch app - Home tab is selected
- [ ] **Test**: Tap Watchlist tab - navigates to Watchlist
- [ ] **Test**: Tap Portfolio tab - navigates to Portfolio
- [ ] **Test**: Tap Home tab - returns to Home
- [ ] **Test**: Selected tab is highlighted in accent color
- [ ] **Test**: Rotate device - current tab is preserved

#### 2.2 Deep Navigation
- [ ] **Test**: Home → analyze stock → Watchlist tab → return maintains state
- [ ] **Test**: Switch tabs rapidly - no crashes
- [ ] **Test**: Navigate away (home button) and return - state preserved

### Phase 3: Home Screen (Stock Analysis) Tests

#### 3.1 Basic Search
- [ ] **Test**: Enter "AAPL" → click Analyze → shows analysis
- [ ] **Test**: Enter "aapl" (lowercase) → converts to "AAPL"
- [ ] **Test**: Enter invalid ticker "XXXZZZ" → shows error
- [ ] **Test**: Leave ticker blank → shows validation error
- [ ] **Test**: Enter "MSFT" while loading AAPL → cancels AAPL request

#### 3.2 Analysis Display
- [ ] **Test**: Successful analysis shows:
  - Company name and ticker
  - Current price and change %
  - Verdict badge (BUY/CAUTION/DO NOT BUY)
  - Key metrics section
  - Fundamental scorecard (if data available)
  - Decision flow
  - LLM opinion (if API key configured)
- [ ] **Test**: Price changes shown in correct color (green=up, red=down)
- [ ] **Test**: Refresh button updates data

#### 3.3 Action Buttons
- [ ] **Test**: "Watchlist" button is visible
- [ ] **Test**: "Portfolio" button is visible
- [ ] **Test**: Clicking "Watchlist" adds stock (verify in Watchlist tab)
- [ ] **Test**: Clicking "Portfolio" opens dialog
- [ ] **Test**: Buttons work after analyzing multiple stocks

### Phase 4: Watchlist Tests

#### 4.1 Empty State
- [ ] **Test**: First launch → Watchlist shows empty state
- [ ] **Test**: Empty state shows icon and helpful text
- [ ] **Test**: Empty state is centered on screen

#### 4.2 Adding to Watchlist
- [ ] **Test**: Add AAPL from Home → appears in Watchlist
- [ ] **Test**: Watchlist item shows:
  - Ticker (AAPL)
  - Company name (Apple Inc.)
  - Current price
  - Price change %
  - Recommendation badge
- [ ] **Test**: Add MSFT → list shows both stocks
- [ ] **Test**: Add same stock twice → shows error or updates existing
- [ ] **Test**: Add 10 stocks → list scrolls properly

#### 4.3 Watchlist Operations
- [ ] **Test**: Pull-to-refresh → shows loading indicator
- [ ] **Test**: Pull-to-refresh → updates prices
- [ ] **Test**: Tap watchlist item → navigates to stock analysis
- [ ] **Test**: Tap delete button → item is removed
- [ ] **Test**: Delete all items → shows empty state again

#### 4.4 Data Persistence
- [ ] **Test**: Add stocks → close app → reopen → stocks still there
- [ ] **Test**: Device restart → watchlist data persists
- [ ] **Test**: Clear app data → watchlist is empty

#### 4.5 Background Sync
- [ ] **Test**: Add stocks → wait 6+ hours → prices update automatically
- [ ] **Test**: Check logs for sync worker execution
- [ ] **Test**: Turn off network → sync fails gracefully
- [ ] **Test**: Turn on network → sync resumes

### Phase 5: Portfolio Tests

#### 5.1 Empty State
- [ ] **Test**: First launch → Portfolio shows empty state
- [ ] **Test**: FAB (+ button) is visible in empty state

#### 5.2 Adding Holdings via Analysis
- [ ] **Test**: Analyze AAPL → click Portfolio button → dialog opens
- [ ] **Test**: Dialog pre-fills:
  - Ticker: AAPL
  - Company name: Apple Inc.
  - Purchase price: Current price
- [ ] **Test**: Enter quantity: 10 → click Add → holding appears
- [ ] **Test**: Enter invalid quantity (negative) → shows error
- [ ] **Test**: Enter invalid price (zero) → shows error
- [ ] **Test**: Leave ticker blank → shows error
- [ ] **Test**: Click Cancel → dialog closes without saving

#### 5.3 Adding Holdings via FAB
- [ ] **Test**: Click FAB → dialog opens
- [ ] **Test**: All fields are empty (no pre-fill)
- [ ] **Test**: Enter all fields manually → saves successfully
- [ ] **Test**: Select purchase date → date picker works
- [ ] **Test**: Enter notes → notes are saved

#### 5.4 Portfolio Display
- [ ] **Test**: Add 1 holding → summary card shows:
  - Total value = (current price × quantity)
  - Cost basis = (purchase price × quantity)
  - Gain/Loss ($ and %)
  - Number of stocks = 1
- [ ] **Test**: Gain/Loss shows green for profit, red for loss
- [ ] **Test**: Trending icon (up/down) matches gain/loss
- [ ] **Test**: Add second holding → totals update correctly

#### 5.5 Holdings Cards
- [ ] **Test**: Holding card shows:
  - Ticker and company name
  - Quantity and purchase price
  - Current value
  - Gain/Loss ($ and %)
  - Current recommendation badge
- [ ] **Test**: Colors match gain/loss
- [ ] **Test**: Tap holding → navigates to analysis
- [ ] **Test**: Tap delete → confirmation or immediate delete

#### 5.6 Portfolio Calculations
- [ ] **Test**: Buy 10 shares @ $150 → price goes to $160 → shows $100 gain (10%)
- [ ] **Test**: Buy 5 shares @ $200 → price goes to $180 → shows -$100 loss (-10%)
- [ ] **Test**: Multiple holdings → total value = sum of all
- [ ] **Test**: Mixed gains/losses → total calculates correctly

#### 5.7 Refresh
- [ ] **Test**: Pull-to-refresh → shows loading
- [ ] **Test**: Refresh updates current prices
- [ ] **Test**: Gain/Loss recalculates after refresh
- [ ] **Test**: Recommendation badges update

#### 5.8 Data Persistence
- [ ] **Test**: Add holdings → close app → reopen → holdings persist
- [ ] **Test**: Portfolio calculations persist correctly

### Phase 6: Database Tests

#### 6.1 Migration
- [ ] **Test**: Install old version (v3) → add data → upgrade to v4 → data intact
- [ ] **Test**: Fresh install creates all tables
- [ ] **Test**: Database version is 4

#### 6.2 Data Integrity
- [ ] **Test**: Add 100 watchlist items → no performance issues
- [ ] **Test**: Add 50 portfolio holdings → calculations still correct
- [ ] **Test**: Concurrent operations → no race conditions
- [ ] **Test**: Database doesn't grow excessively

### Phase 7: Error Handling Tests

#### 7.1 Network Errors
- [ ] **Test**: Turn off WiFi/data → analyze stock → shows error message
- [ ] **Test**: Slow network → shows loading state
- [ ] **Test**: Network timeout → shows timeout error
- [ ] **Test**: Invalid API response → handles gracefully

#### 7.2 Invalid Input
- [ ] **Test**: Special characters in ticker → handled
- [ ] **Test**: Very long ticker → truncated or rejected
- [ ] **Test**: Negative quantity in portfolio → rejected
- [ ] **Test**: Future date in portfolio → allowed or rejected?

#### 7.3 Edge Cases
- [ ] **Test**: Stock with no data → shows partial info
- [ ] **Test**: Stock with missing company name → uses ticker
- [ ] **Test**: Zero price → handles division by zero
- [ ] **Test**: Very large numbers → formats correctly

### Phase 8: UI/UX Tests

#### 8.1 Responsiveness
- [ ] **Test**: App launches in < 2 seconds
- [ ] **Test**: Navigation transitions are smooth
- [ ] **Test**: Scrolling is smooth (60fps)
- [ ] **Test**: No UI freezes during operations

#### 8.2 Accessibility
- [ ] **Test**: TalkBack reads all elements
- [ ] **Test**: Content descriptions present
- [ ] **Test**: Contrast ratios meet WCAG AA
- [ ] **Test**: Text size scales with system settings
- [ ] **Test**: All interactive elements > 48dp touch target

#### 8.3 Theming
- [ ] **Test**: Light mode displays correctly
- [ ] **Test**: Dark mode displays correctly
- [ ] **Test**: System theme setting is respected
- [ ] **Test**: Dynamic colors work on Android 12+ (Material You)

#### 8.4 Orientation
- [ ] **Test**: Portrait mode works
- [ ] **Test**: Landscape mode works
- [ ] **Test**: Rotation preserves state
- [ ] **Test**: All content visible in both orientations

### Phase 9: Performance Tests

#### 9.1 Memory
- [ ] **Test**: Memory usage < 200MB under normal use
- [ ] **Test**: No memory leaks after 30 minutes
- [ ] **Test**: Background sync doesn't spike memory

#### 9.2 Battery
- [ ] **Test**: Battery drain < 2% per hour (idle)
- [ ] **Test**: Background sync uses < 1% battery
- [ ] **Test**: No wakelocks keeping device awake

#### 9.3 Storage
- [ ] **Test**: App size < 50MB
- [ ] **Test**: Database size reasonable (< 10MB for 100 items)
- [ ] **Test**: Cache is cleaned properly

### Phase 10: Stress Tests

- [ ] **Test**: Add 1000 items to watchlist → performance
- [ ] **Test**: Add 500 portfolio holdings → calculations
- [ ] **Test**: Rapid navigation (100 tab switches) → no crash
- [ ] **Test**: Analyze 50 stocks in a row → no crash
- [ ] **Test**: Long running (24 hours) → no crashes

## Manual Testing Checklist

### Day 1: Basic Functionality
1. Install app
2. Test navigation
3. Analyze 5 different stocks
4. Add 3 stocks to watchlist
5. Add 2 holdings to portfolio
6. Verify all data displays correctly

### Day 2: Operations
1. Refresh watchlist
2. Refresh portfolio
3. Delete items from both
4. Add items again
5. Navigate between screens
6. Test pull-to-refresh

### Day 3: Persistence & Edge Cases
1. Close and reopen app
2. Test with no network
3. Test with slow network
4. Test invalid inputs
5. Test with empty states
6. Restart device

### Day 4: Background & Performance
1. Let app run for 6+ hours
2. Check background sync logs
3. Monitor battery usage
4. Check memory usage
5. Test with many items

## Automated Testing

Once Java is set up, run:

```bash
# Unit tests
./gradlew.bat :app:test

# UI tests (requires emulator/device)
./gradlew.bat :app:connectedAndroidTest

# Generate coverage report
./gradlew.bat :app:jacocoTestReport
```

## Bug Reporting Template

When you find issues:

```markdown
**Bug Title**: [Short description]

**Severity**: Critical / High / Medium / Low

**Steps to Reproduce**:
1. Step one
2. Step two
3. Step three

**Expected Behavior**:
What should happen

**Actual Behavior**:
What actually happened

**Screenshots**:
[Attach if relevant]

**Device Info**:
- Device: [Pixel 6, Samsung S23, etc.]
- Android Version: [13, 14, etc.]
- App Version: [1.0.0]

**Logs**:
```
Logcat output if available
```
```

## Test Results Summary Template

```markdown
# Test Results - [Date]

## Environment
- Device:
- Android Version:
- Build: app-debug.apk

## Summary
- Total Tests:
- Passed: ✅
- Failed: ❌
- Skipped: ⏭️

## Failed Tests
1. [Test name] - [Reason]
2. [Test name] - [Reason]

## Performance Metrics
- App Launch Time:
- Memory Usage:
- Battery Drain:
- Database Size:

## Notes
[Any additional observations]
```

## Next Steps After Testing

1. **If tests pass**: Ready for beta release
2. **If tests fail**: Document bugs, prioritize fixes
3. **Performance issues**: Profile with Android Profiler
4. **Crashes**: Check Logcat, add error logging
5. **UI issues**: Refine designs, improve layouts

## Getting Help

- **Build Issues**: Check gradle logs in `app/build/outputs/logs/`
- **Runtime Crashes**: Use `adb logcat` to view logs
- **UI Issues**: Use Android Studio Layout Inspector
- **Performance**: Use Android Profiler in Android Studio
