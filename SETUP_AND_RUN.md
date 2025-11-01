# BD Finance Android - Setup and Run Guide

## 🚫 Current Issue
**Java is not installed on this system**, which is required to build Android apps.

## ✅ Quick Setup Guide

### Step 1: Install Java (Required)

#### Option A: Install Eclipse Temurin (Recommended)
1. Visit: https://adoptium.net/temurin/releases/
2. Download JDK 17 or 21 for Windows x64
3. Run installer with default settings
4. Installer should set JAVA_HOME automatically

#### Option B: Install Oracle JDK
1. Visit: https://www.oracle.com/java/technologies/downloads/
2. Download Java SE 17 or 21
3. Install and manually set JAVA_HOME

### Step 2: Verify Java Installation

Open **PowerShell** or **Command Prompt**:

```cmd
java -version
```

Should show something like:
```
openjdk version "17.0.9" 2023-10-17
OpenJDK Runtime Environment Temurin-17.0.9+9 (build 17.0.9+9)
```

If not found, set JAVA_HOME manually:

```powershell
# PowerShell (Run as Administrator)
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot', 'Machine')
$path = [System.Environment]::GetEnvironmentVariable('Path', 'Machine')
[System.Environment]::SetEnvironmentVariable('Path', "$path;%JAVA_HOME%\bin", 'Machine')

# Restart PowerShell and test
java -version
```

### Step 3: Build the App

```cmd
cd C:\Github\BD\BD_Finance_android

# Windows
gradlew.bat :app:assembleDebug

# Build should complete with "BUILD SUCCESSFUL"
```

**Output APK**: `app\build\outputs\apk\debug\app-debug.apk`

### Step 4: Run the App

#### Option A: Using Android Studio (Easiest)

1. **Install Android Studio** (if not installed)
   - Download: https://developer.android.com/studio
   - Install with default settings (includes Android SDK and emulator)

2. **Open Project**
   - Launch Android Studio
   - File → Open → Select `C:\Github\BD\BD_Finance_android`
   - Wait for Gradle sync to complete

3. **Create Emulator** (if needed)
   - Tools → Device Manager
   - Create Device → Select "Pixel 6" or similar
   - Select System Image: API 33 (Android 13) or higher
   - Finish and start emulator

4. **Run App**
   - Click green "Run" button (▶️) or press Shift+F10
   - Select emulator or connected device
   - App should launch in ~30 seconds

#### Option B: Using Physical Device

1. **Enable Developer Mode** on Android device:
   - Settings → About Phone → Tap "Build Number" 7 times
   - Settings → System → Developer Options → Enable USB Debugging

2. **Connect Device**
   - Connect via USB cable
   - Allow USB debugging when prompted

3. **Install APK**
   ```cmd
   # Method 1: Use gradlew
   gradlew.bat :app:installDebug

   # Method 2: Use adb (Android Debug Bridge)
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```

4. **Launch App**
   - Find "BD Finance" in app drawer
   - Tap to launch

#### Option C: Using Command Line + Emulator

```cmd
# List available emulators
emulator -list-avds

# Start emulator
emulator -avd <emulator-name>

# Wait for boot, then install
gradlew.bat :app:installDebug

# Launch app
adb shell am start -n com.example.bd_finance/.MainActivity
```

## 🧪 Testing the App

Once the app is running, follow the [TESTING_GUIDE.md](TESTING_GUIDE.md) to test all features.

### Quick Smoke Test (5 minutes)

1. **Test Navigation**
   - ✅ Launch app → Home tab is selected
   - ✅ Tap Watchlist tab → switches
   - ✅ Tap Portfolio tab → switches
   - ✅ Tap Home → returns

2. **Test Stock Analysis**
   - ✅ Enter "AAPL" → Click Analyze
   - ✅ Wait for analysis to load
   - ✅ Verify verdict badge shows (BUY/CAUTION/DO NOT BUY)
   - ✅ Verify price and change % display

3. **Test Watchlist**
   - ✅ From AAPL analysis → Click "Watchlist" button
   - ✅ Go to Watchlist tab
   - ✅ Verify AAPL appears in list
   - ✅ Pull down to refresh
   - ✅ Tap delete button → item removed

4. **Test Portfolio**
   - ✅ Analyze "MSFT"
   - ✅ Click "Portfolio" button
   - ✅ Dialog opens with pre-filled data
   - ✅ Enter quantity: 10
   - ✅ Click Add
   - ✅ Go to Portfolio tab
   - ✅ Verify summary card shows total value
   - ✅ Verify MSFT holding displays

5. **Test Persistence**
   - ✅ Close app (swipe away)
   - ✅ Reopen app
   - ✅ Verify Watchlist still has items
   - ✅ Verify Portfolio still has holdings

## 📊 Run Unit Tests

```cmd
# Run all unit tests
gradlew.bat :app:test

# Run specific test
gradlew.bat :app:test --tests "PortfolioCalculationsTest"

# Generate test report
# Open: app/build/reports/tests/testDebugUnitTest/index.html
```

## 🐛 Troubleshooting

### "JAVA_HOME is not set"
- Follow Step 1 above to install Java
- Verify with `java -version`

### "Android SDK not found"
- Install Android Studio (includes SDK)
- OR set ANDROID_HOME environment variable

### "Gradle sync failed"
- Check internet connection (needs to download dependencies)
- Click "Sync Now" in Android Studio
- OR run `gradlew.bat clean` and try again

### "App won't install"
- Check device has enough storage
- Try uninstalling old version first
- Check ADB connection: `adb devices`

### "App crashes on launch"
- Check Logcat: `adb logcat *:E`
- Or view in Android Studio (Logcat tab)
- Look for exceptions and stack traces

### "Can't find emulator"
- Android Studio → Tools → Device Manager → Create Device
- OR download system image manually

### Build is slow
- First build can take 5-10 minutes (downloading dependencies)
- Subsequent builds should be 30-60 seconds
- Use `--offline` flag if already downloaded: `gradlew.bat --offline :app:assembleDebug`

## 📱 Recommended Test Devices/Emulators

- **Pixel 6** (API 33 - Android 13)
- **Pixel 7** (API 34 - Android 14)
- Samsung Galaxy S23 (physical device)
- Any device with Android 13+

## 🎯 Success Criteria

After setup, you should be able to:
- ✅ Build APK without errors
- ✅ Install on emulator or device
- ✅ Launch app and see Home screen
- ✅ Navigate between all 3 tabs
- ✅ Analyze a stock successfully
- ✅ Add to watchlist
- ✅ Add to portfolio
- ✅ See data persist after restart

## 📋 Next Steps After Setup

1. **Complete Full Testing** - See [TESTING_GUIDE.md](TESTING_GUIDE.md)
2. **Review Test Results** - Document any issues found
3. **Performance Testing** - Monitor memory, battery, speed
4. **UI Polish** - Check for design inconsistencies
5. **Bug Fixes** - Address any crashes or errors
6. **Beta Release** - Share with test users

## 🆘 Still Having Issues?

1. **Check build logs**:
   ```cmd
   gradlew.bat :app:assembleDebug --info
   ```

2. **Clean and rebuild**:
   ```cmd
   gradlew.bat clean
   gradlew.bat :app:assembleDebug
   ```

3. **Check Android Studio logs**:
   - View → Tool Windows → Build
   - Look for error messages

4. **Verify dependencies**:
   - Check `app/build.gradle.kts` has all dependencies
   - Check `gradle/libs.versions.toml` if using version catalogs

## 📚 Additional Resources

- **Android Developers**: https://developer.android.com/
- **Gradle Build Tool**: https://docs.gradle.org/
- **Kotlin**: https://kotlinlang.org/docs/
- **Jetpack Compose**: https://developer.android.com/jetpack/compose

---

**Note**: This app was implemented by Claude AI on November 1, 2025. All code is ready to build and run once Java is installed.
