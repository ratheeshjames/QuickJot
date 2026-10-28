package com.example.domain.repository

import com.example.ui.theme.AccentColorChoice
import kotlinx.coroutines.flow.Flow

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class ViewMode { LIST, GRID, COMPACT }
enum class SortOption { MODIFIED, CREATED, TITLE, COLOR }

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val accentChoice: AccentColorChoice = AccentColorChoice.PURPLE,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOption: SortOption = SortOption.MODIFIED,
    val sortAscending: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val biometricEnabled: Boolean = false,
    val pinHash: String? = null,
    val trashRetentionDays: Int = 14,
    val characterLimit: Int = 1000,
    val screenshotBlocked: Boolean = false,
    val hideInRecents: Boolean = false,
    val autoLockTimeoutSeconds: Int = 0,
    val backupFolderUri: String? = null,
    val backupSchedule: String = "OFF"
)

/**
 * Domain interface for User Preferences backed by DataStore.
 */
interface UserPreferencesRepository {
    val preferencesFlow: Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setAccentChoice(accent: AccentColorChoice)
    suspend fun setViewMode(mode: ViewMode)
    suspend fun setSortOption(option: SortOption, ascending: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setPinHash(hash: String?)
    suspend fun setTrashRetentionDays(days: Int)
    suspend fun setCharacterLimit(limit: Int)
    suspend fun setScreenshotBlocked(blocked: Boolean)
    suspend fun setHideInRecents(hide: Boolean)
    suspend fun setAutoLockTimeoutSeconds(seconds: Int)
    suspend fun setBackupFolderUri(uri: String?)
    suspend fun setBackupSchedule(schedule: String)
}
