# QuickJot 📝⚡

> **Lightning-fast, distraction-free notes app designed to keep up with your thoughts.**  
> Built with **Kotlin**, **Jetpack Compose**, and the latest **Material 3 Expressive** design system.

[![Android API](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203%20Expressive-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Offline](https://img.shields.io/badge/Privacy-100%25%20Offline-green)](https://github.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 🔒 100% Offline & Private by Design

QuickJot was built from the ground up on a zero-trust, privacy-first foundation:
- **Zero Network Permission**: The `android.permission.INTERNET` permission is **strictly absent** from the manifest. QuickJot physically cannot send your notes to any server.
- **Hardware-Backed Database Encryption**: All notes, tags, checklists, and revision histories are stored in a local SQLite database encrypted with **SQLCipher (AES-256)**. The encryption key is securely protected by the **Android Keystore**.
- **Biometric & PIN Lock**: Guard your notes behind a 4-digit salted PIN (SHA-256) and system `BiometricPrompt` (fingerprint or face unlock).
- **Per-Note Locking**: Lock sensitive notes individually so their previews remain hidden until authenticated.
- **Screen & App Switcher Protection**: Optional `FLAG_SECURE` prevents screenshots/recordings and hides previews in the Android Recent Tasks switcher.

---

## ✨ Features at a Glance

### 🚀 A. Lightning Fast Creation
- **Expressive FAB Menu**: Quick actions to compose a standard note, create an interactive checklist, dictate via speech-to-text, or paste directly from clipboard.
- **System Integrations**:
  - **Glance AppWidget**: 1-tap capture and recent notes on your home screen.
  - **Quick Settings Tile**: Pull down the Android notification shade to jot from anywhere.
  - **Launcher App Shortcuts**: Long-press app icon to quickly create a note, checklist, or launch search.
  - **Android Share Target**: Share text or links from web browsers or any other app directly into QuickJot via `ACTION_SEND`.
- **Typing Auto-Save & Draft Safety**: Notes save in real time as you type with zero delay. Empty scratchpads are cleaned up automatically without cluttering your library.
- **Configurable Character Limit**: Configurable limit (500, 1,000, 2,000, 5,000, or Unlimited) with live counter badge.

### ✍️ B. Rich Formatting & Note Editor
- **Rich Text & Lists**: Bold, italic, underline, strikethrough, bullet lists, numbered lists, and interactive checklists.
- **List Ergonomics**: Auto-continuation on Enter, drag-and-drop checklist item reordering, and instant strike-through upon completion.
- **In-Editor Find & Replace**: Live match counter with single and replace-all capabilities.
- **Snapshot Version History**: Automatic snapshots saved as you edit. Revert to any previous revision with a single tap.
- **Smart Linkification**: Automatic clickable links for URLs, phone numbers, and email addresses.
- **Read-Only Mode & Metadata**: Word and character counts, created timestamps, and last-modified dates.

### 🔍 C. FTS4 Search & Organization
- **Instant Full-Text Search (FTS4)**: Sub-millisecond SQLite FTS indexing across titles and note bodies with search term match highlighting.
- **Multi-Filter Chips**: Filter by Pinned, Checklists, Reminders, Locked status, 12 Color Labels, Tags, and Date ranges.
- **12 Material Tonal Colors**: Map notes to color palettes optimized for both light and dark modes.
- **Folders & Nested Subfolders**: Organize thoughts in nested folder hierarchies with easy move-to-folder dialogs.
- **Tags System**: `#tag` detection with autocomplete, tag management, rename, and tag merging.
- **Bulk Operations**: Long-press multi-select to bulk color, pin/unpin, archive, move to folder, or move to trash.
- **View Modes**: Toggle between List, Grid, and Compact layouts; sort by Modified, Created, Title, or Color (Ascending/Descending).

### ⏰ D. Punctual Reminders & Exact Alarms
- **One-Time & Recurring**: Daily, weekly, monthly, or custom reminder schedules.
- **Actionable Notifications**: "Snooze (15m)" and "Mark Done" buttons directly in the notification shade.
- **Reboot Persistence**: `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED` broadcast receivers re-schedule active alarms across device restarts.
- **Android 12+ Permission Flow**: Seamless `SCHEDULE_EXACT_ALARM` handling with battery-friendly fallback.

### 🗑️ E. Trash, Recovery & Retention
- **Swipe-to-Trash**: Swipe left on any note card to move to Trash with instant Undo snackbar.
- **Configurable Retention**: Choose 7, 14, or 30 days retention policy.
- **WorkManager Purge**: Automated daily background job safely purges expired trash notes.
- **Permanent Deletion & Empty Trash**: Delete individual items forever or empty entire trash with confirmation dialogs.

### 💾 F. Backup, Export & Restore
- **Storage Access Framework (SAF)**: Export `.qjbak` backup files to any user-chosen local or SD-card directory without requiring broad storage permissions.
- **AES-256-GCM Password Encryption**: Optional PBKDF2 + AES-GCM encryption for backup files.
- **Automated Scheduled Backups**: Daily or weekly background backup jobs via `WorkManager`.
- **Markdown & Plain Text Export**: Export individual or grouped notes as standard `.md` or `.txt` files.

### 📊 G. Statistics & Library Insights
- **Hero Dashboard**: Real-time stats on total active notes, word counts, and characters written.
- **Task Analytics**: Checklist tasks progress bar with completion percentages.
- **Reading Time Estimates**: Average words per note and estimated reading times.

---

## 🏗️ Architecture & Technology Stack

```
com.example
├── data
│   ├── backup         # BackupManager, ScheduledBackupWorker, TrashPurgeWorker, DTOs
│   ├── datastore      # Preferences DataStore repository (theme, security, limits)
│   ├── local          # Room Database, SQLCipher open helper, DAOs, Entities, FTS
│   ├── repository     # OfflineNotesRepository implementation
│   └── security       # DatabaseKeyManager (Keystore), SecurityManager (PIN, Biometrics)
├── di                 # AppContainer & ViewModelFactory (Constructor Injection)
├── domain             # Domain Models & Repository Contracts
├── receivers          # ReminderReceiver, BootReceiver
├── services           # QuickJotTileService (Quick Settings)
├── ui
│   ├── archive        # Archive screen & view model
│   ├── components     # Reusable M3 Expressive pickers, dialogs, empty states
│   ├── editor         # Note/Checklist editor, formatting toolbar, version history
│   ├── folders        # Folder hierarchy browser
│   ├── help           # Help, gestures, and tips guide
│   ├── home           # Note list/grid, FAB menu, drawer, swipe actions
│   ├── navigation     # Type-safe Navigation Compose routes
│   ├── onboarding     # 4-step onboarding flow & permission prompts
│   ├── reminders      # Reminders screen with overdue/today/upcoming grouping
│   ├── search         # FTS Search with live filters
│   ├── security       # Lock screen PIN keypad & biometric prompt
│   ├── settings       # Appearance, Security, Trash, and Backup configuration
│   ├── stats          # Statistics & writing insights dashboard
│   ├── tags           # Tag management & tag merging
│   ├── theme          # MaterialExpressiveTheme, Color, Shapes, Typography
│   ├── trash          # Trash management & restore
│   └── util           # HapticFeedbackHelper
└── widget             # Jetpack Glance AppWidget & AppWidgetReceiver
```

- **UI Framework**: Jetpack Compose with Material 3 Expressive (`MaterialExpressiveTheme`, `MotionScheme.expressive()`).
- **Database**: Android Jetpack Room with Zetetic SQLCipher.
- **Preferences**: Jetpack DataStore Preferences.
- **Background Jobs**: Jetpack WorkManager (`CoroutineWorker`).
- **Home Widget**: Jetpack Glance Material3.
- **Serialization**: `kotlinx.serialization.json`.
- **Testing**: JUnit & Robolectric for local JVM execution.

---

## 📦 Releases & APK Download

Pre-compiled, production-ready release APKs are located in the **`Generated APKs`** directory:

| Artifact | File Name | Size | SHA-256 Hash |
| :--- | :--- | :--- | :--- |
| **Release APK (v1.0.0)** | [`Generated APKs/quickjot-v1.0.0-release.apk`](Generated%20APKs/quickjot-v1.0.0-release.apk) | ~37 MB | Signed & Verified |

### Installing the APK on Android
1. Download `quickjot-v1.0.0-release.apk` to your Android device (Android 7.0 / API 24 or newer).
2. Tap the downloaded APK in your device's Files/Downloads app.
3. Allow installation from unknown sources if prompted.
4. Launch **QuickJot** and enjoy fast, private note-taking!

---

## 🛠️ Building from Source

### Prerequisites
- JDK 17 or newer
- Android SDK with API 36 (`compileSdk = 36`)
- Gradle (provided in build environment)

### Build Commands
```bash
# Compile and run unit & Robolectric tests
gradle :app:testDebugUnitTest

# Assemble Debug APK
gradle assembleDebug

# Assemble Release APK (requires release keystore)
STORE_PASSWORD=quickjotpassword KEY_PASSWORD=quickjotpassword KEYSTORE_PATH=my-upload-key.jks gradle assembleRelease
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
