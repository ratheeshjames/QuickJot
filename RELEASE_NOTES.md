# QuickJot v1.0.0 Release Notes

**Release Name:** QuickJot v1.0.0 – Lightning-Fast, 100% Offline Notes  
**Tag:** `v1.0.0`  
**Target Commit:** `main`  
**Minimum Android Version:** Android 7.0 (Nougat, API 24)  
**Target Android Version:** Android 16 (API 36)

---

## 🚀 Overview

We are thrilled to present the initial production release of **QuickJot**, a personal short-notes application designed for maximum speed, tactile delight, and uncompromising privacy.

QuickJot operates with **zero network permissions** and requires no account or cloud sync. All data is encrypted locally using **SQLCipher (AES-256)** with encryption keys held safely inside the **Android Keystore**.

---

## 🌟 What's Included in v1.0.0

### ⚡ Fast Note Capture & Intake
- **Expressive FAB Menu**: 1-tap creation for standard notes, checklists, voice-to-text dictation, and clipboard pasting.
- **Glance Home Screen Widget**: Clean Material 3 widget with quick note jotting directly from your launcher.
- **Quick Settings Tile**: Android notification shade tile to capture a thought from any screen without switching apps.
- **Launcher Shortcuts**: Long-press QuickJot icon for instant shortcuts to New Note, Checklist, or Search.
- **System Share Target**: Send text or links from any browser or app directly to QuickJot via `ACTION_SEND`.
- **Instant Auto-Save & Empty Scratchpad Cleanup**: Never lose a thought; blank notes are pruned automatically.

### 📝 Rich Formatting & Note Editor
- **Material 3 Formatting Toolbar**: Bold, italic, underline, strikethrough, bullet lists, numbered lists, and interactive checklists.
- **List Ergonomics**: Auto-continue list items on Enter; drag-and-drop to reorder checklist items.
- **In-Editor Find & Replace**: Live match navigation and replace-all functionality.
- **Snapshot Version History**: Automatic snapshots saved during editing with 1-tap restoration.
- **Clickable Links**: Automatic link detection for URLs, phone numbers, and emails.
- **Live Counters**: Real-time word count and configurable character limits (500, 1000, 2000, 5000, or unlimited).

### 🔍 FTS4 Search & Organization
- **Instant Full-Text Search**: SQLite FTS4 indexed searches across titles and note bodies with term highlighting.
- **Advanced Filtering**: Filter by pinned, checklists, locked, reminders, 12 label colors, tags, and date ranges.
- **12 Tonal Note Colors**: Designed for optimal readability across light and dark themes.
- **Hierarchical Folders**: Create subfolders and easily move notes between categories.
- **Tag Management**: Automatic `#tag` extraction, autocomplete, tag renaming, and tag merging.
- **Multi-Select Bulk Actions**: Bulk color change, pin/unpin, archive, move to folder, or move to trash.
- **Layouts & Sorting**: Switch between List, Grid, and Compact views; sort by modified, created, title, or color.

### ⏰ Exact Alarms & Reminders
- **One-Time & Recurring**: Daily, weekly, monthly, or custom reminder frequencies.
- **Actionable Notifications**: "Snooze (15m)" and "Mark Done" buttons directly from the notification shade.
- **Device Reboot Resilience**: Broadcast receivers restore all pending alarms across system restarts.

### 🛡️ Security & Privacy
- **100% Offline Guarantee**: `android.permission.INTERNET` is strictly omitted from the manifest.
- **Encrypted Local Storage**: SQLCipher AES-256 Room database with Android Keystore master key.
- **App & Note Lock**: 4-digit PIN authentication with salt, plus fingerprint/face biometric unlock.
- **Confidential Notes**: Per-note lock to obscure content until authenticated.
- **Screen Privacy**: Optional screenshot blocking (`FLAG_SECURE`) and recents task preview blanking.

### 💾 Backup, Export & Trash
- **SAF Backup & Restore**: Export full JSON database backups (`.qjbak`) to any local or SD storage location.
- **Optional AES-256-GCM Password Encryption**: Encrypt your backup files with a password before saving.
- **Automated Scheduled Backups**: Daily or weekly background backup scheduling via `WorkManager`.
- **Markdown & Plain Text Export**: Share or export individual notes as `.md` or `.txt`.
- **Configurable Trash Retention**: Choose 7, 14, or 30 days retention with automated background cleanup.

### 🎨 Personalization & Statistics
- **Theming**: Dynamic Material You theming on Android 12+ or 6 custom accent palettes (Royal Purple, Electric Blue, Emerald Green, Crimson Rose, Warm Amber, Deep Slate).
- **Library Insights**: Writing metrics, word counts, character counts, checklist completion progress, and reading time estimates.

---

## 📦 Release Assets

- **`quickjot-v1.0.0-release.apk`**: Production release APK signed with RSA-2048 key.
- **File size**: ~37 MB
- **Architecture**: Universal (arm64-v8a, armeabi-v7a, x86, x86_64)

### Verification
- **Package Name**: `com.example`
- **Application ID**: `com.aistudio.quickjot.app`
- **Version Code**: `1`
- **Version Name**: `1.0`
