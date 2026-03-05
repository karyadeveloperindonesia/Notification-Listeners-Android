# Quick Start Guide

## 🚀 Installation (30 seconds)

### Option 1: Build & Install Yourself

**Prerequisites:**
- Android SDK Platform 36
- Kotlin 2.0.21+
- Min 80MB free space

**Build:**
```bash
cd /Users/allmediaindo/AndroidStudioProjects/NotificationListeners
./gradlew assembleDebug
```

**Install:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Use Pre-built APK

**Download:**
- Debug: `app/build/outputs/apk/debug/app-debug.apk` (54 MB)
- Release: `app/build/outputs/apk/release/app-release.apk` (39 MB)

**Install:**
```bash
adb install app-debug.apk
# or
adb install app-release.apk
```

---

## ⚙️ Setup (2 minutes)

### Step 1: Open Calculator App
Tap the "Notification Capture" app icon — should show a calculator UI

### Step 2: Grant Notification Access
**Red banner will appear:**
> "Notification Access Required"

Tap **"Open Settings"**

### Step 3: Enable Permission
1. Find **"NotificationListeners"** in the list
2. Toggle the switch **ON** (turns green)
3. Tap **"Allow"** on the security warning

### Step 4: Verify Permission
- Return to app
- Tap **"Refresh"** button
- Green banner appears: **"Listener Active"**

---

## 🔐 How to Access Hidden Features

### Secret Code: `231199`

**To Activate:**
1. Open calculator app
2. Type any calculation (e.g., "5 + 3")
3. Tap these digits in order: **2 → 3 → 1 → 1 → 9 → 9**
4. After the 6th tap, automatically navigate to notification history

**Note:** No visual feedback when code is detected. It just happens.

### What You'll See
A full history of ALL notifications from every app:
- Messages (WhatsApp, Telegram, SMS)
- Email (Gmail, Outlook)
- Bank alerts
- Social media
- Everything!

---

## 📊 Testing the App

### Generate Test Notifications
```bash
# Send SMS notification
adb shell am start -a android.intent.action.SENDTO -d sms:1234567890 --es sms_body "Test message"

# Or use any app on device:
# - Send yourself a message on WhatsApp
# - Send yourself an email
# - Get a push notification from any app
```

### View Logs in Real-Time
```bash
adb logcat -s NotifCaptureService
```

**You'll see output like:**
```
╔══════════════════════════════════════════
║ CAPTURED NOTIFICATION
╠══════════════════════════════════════════
║ App:     WhatsApp
║ Title:   John Doe
║ Content: Hey! How are you?
║ Time:    1741075123456
╚══════════════════════════════════════════
```

---

## 📱 What Can Be Captured

This app can see notifications from:

| App Type | What Gets Captured |
|----------|---|
| **Messages** | SMS previews, chat message text |
| **Email** | Subject + message preview |
| **Banking** | Transaction alerts, balance updates, OTPs |
| **Social Media** | Friend requests, message previews, @ mentions |
| **Shopping** | Order confirmations, delivery updates |
| **Maps/Navigation** | Route alerts, ETA changes |
| **News** | News headlines and breaking alerts |

### Critical Data at Risk
- 🔴 **2FA/OTP codes** (most critical!)
- 🔴 **Password reset links**
- 🔴 **Login credentials in notifications**
- 🔴 **Financial transaction details**
- 🔴 **Sensitive messages**

---

## 🛡️ Defensive Measures

### Disable Notification Previews
```
Settings → Apps & Notifications → Notifications
→ Lock screen → Hide sensitive content
```

### Check Notification Listeners
```
Settings → Apps → Special app access → Notification access
```
**Review** which apps have access. Remove untrusted ones.

### App-Specific Privacy
- **WhatsApp**: Settings → Notifications → disable message preview
- **Gmail**: Settings → Notifications → disable peek preview
- **Banking Apps**: Enable "Always require PIN"

### Defense Tools
- Install Google Play Protect (scans for malicious apps)
- Use Malwarebytes Mobile Security
- Regular security audits of installed apps

---

## 🔍 Proof of Concept Validation

### To Verify It Works:
1. **Install app**
2. **Grant notification access**
3. **Send yourself a WhatsApp message**
4. **Immediately enter secret code (231199)**
5. **Check notification history** — the message you just sent should be there!

---

## 📂 Project Structure

```
NotificationListeners/
├── app/
│   ├── build.gradle.kts          (dependencies)
│   ├── src/main/
│   │   ├── AndroidManifest.xml   (permissions, activities)
│   │   ├── java/...
│   │   │   ├── MainActivity.kt            (calculator)
│   │   │   ├── service/
│   │   │   │   └── NotificationCaptureService.kt  (listener)
│   │   │   ├── viewmodel/
│   │   │   │   ├── CalculatorViewModel.kt         (new)
│   │   │   │   └── NotificationViewModel.kt
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── CalculatorScreen.kt        (new)
│   │   │   │   │   └── NotificationListScreen.kt
│   │   │   │   └── activity/
│   │   │   │       └── NotificationHistoryActivity.kt  (new)
│   │   │   └── data/
│   │   │       ├── db/
│   │   │       ├── repository/
│   │   └── res/
│   │       ├── values/
│   │       └── xml/
│   └── build/
│       └── outputs/apk/
│           ├── debug/
│           │   └── app-debug.apk
│           └── release/
│               └── app-release.apk
├── gradle/
│   └── libs.versions.toml
├── STEALTH_CALCULATOR_README.md   (full documentation)
└── SECURITY_RESEARCH_README.md    (original docs)
```

---

## 🎯 Key Files to Review

| File | Purpose |
|------|---------|
| [CalculatorViewModel.kt](app/src/main/java/com/putra/notificationlisteners/viewmodel/CalculatorViewModel.kt) | Secret code detection + calculator logic |
| [CalculatorScreen.kt](app/src/main/java/com/putra/notificationlisteners/ui/screens/CalculatorScreen.kt) | Modern calculator UI |
| [NotificationCaptureService.kt](app/src/main/java/com/putra/notificationlisteners/service/NotificationCaptureService.kt) | Background notification listener |
| [NotificationHistoryActivity.kt](app/src/main/java/com/putra/notificationlisteners/ui/activity/NotificationHistoryActivity.kt) | Hidden activity with notification list |
| [AndroidManifest.xml](app/src/main/AndroidManifest.xml) | Permission declarations |

---

## ⚡ Troubleshooting

### App Crashes on Launch
```bash
adb logcat -s "*:E"
```
Check logs for missing dependencies or resource issues.

### Notifications Not Captured
1. Verify permission is enabled: `adb shell settings get secure enabled_notification_listeners`
2. May need to restart app or toggle permission off/on
3. Some ROMs have aggressive notification listener restrictions

### Permission Won't Enable
Device may have MDM (Mobile Device Management) policy blocking it.
Try:
```bash
adb shell cmd notification allow_listener \
  com.putra.notificationlisteners/com.putra.notificationlisteners.service.NotificationCaptureService
```

### Secret Code Not Working
- Ensure you're entering digits slowly (let each tap register)
- Code buffer only remembers last 6 digits
- Try clearing calculation first (tap C)

---

## 📞 Support

For issues:
1. Check app logs: `adb logcat`
2. Review README files in project
3. Verify Android SDK is correct version
4. Test on different device/emulator

---

## ⚖️ Legal & Ethical

**This app is for:**
✅ Educational purposes
✅ Cybersecurity research
✅ Authorized security testing
✅ Understanding malware threats

**This app is NOT for:**
❌ Unauthorized data collection
❌ Spying on others
❌ Violating privacy laws
❌ Malicious distribution

**Use only on devices you own with full consent.** Unauthorized use may violate laws including CFAA, GDPR, HIPAA, and others.

---

Ready to test? 🚀
