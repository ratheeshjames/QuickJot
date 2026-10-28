package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.repository.SortOption
import com.example.domain.repository.ThemeMode
import com.example.domain.repository.UserPreferences
import com.example.domain.repository.UserPreferencesRepository
import com.example.domain.repository.ViewMode
import com.example.ui.theme.AccentColorChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quickjot_preferences")

class DefaultUserPreferencesRepository(private val context: Context) : UserPreferencesRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ACCENT_CHOICE = stringPreferencesKey("accent_choice")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val SORT_ASCENDING = booleanPreferencesKey("sort_ascending")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val TRASH_RETENTION_DAYS = intPreferencesKey("trash_retention_days")
        val CHARACTER_LIMIT = intPreferencesKey("character_limit")
        val SCREENSHOT_BLOCKED = booleanPreferencesKey("screenshot_blocked")
        val HIDE_IN_RECENTS = booleanPreferencesKey("hide_in_recents")
        val AUTO_LOCK_TIMEOUT_SECONDS = intPreferencesKey("auto_lock_timeout_seconds")
        val BACKUP_FOLDER_URI = stringPreferencesKey("backup_folder_uri")
        val BACKUP_SCHEDULE = stringPreferencesKey("backup_schedule")
    }

    override val preferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeMode = when (preferences[PreferencesKeys.THEME_MODE]) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
            val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
            val accentChoice = preferences[PreferencesKeys.ACCENT_CHOICE]?.let {
                try {
                    AccentColorChoice.valueOf(it)
                } catch (e: Exception) {
                    AccentColorChoice.PURPLE
                }
            } ?: AccentColorChoice.PURPLE

            val viewMode = when (preferences[PreferencesKeys.VIEW_MODE]) {
                ViewMode.GRID.name -> ViewMode.GRID
                ViewMode.COMPACT.name -> ViewMode.COMPACT
                else -> ViewMode.LIST
            }
            val sortOption = when (preferences[PreferencesKeys.SORT_OPTION]) {
                SortOption.CREATED.name -> SortOption.CREATED
                SortOption.TITLE.name -> SortOption.TITLE
                SortOption.COLOR.name -> SortOption.COLOR
                else -> SortOption.MODIFIED
            }
            val sortAscending = preferences[PreferencesKeys.SORT_ASCENDING] ?: false
            val hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
            val onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
            val biometricEnabled = preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
            val pinHash = preferences[PreferencesKeys.PIN_HASH]
            val trashRetentionDays = preferences[PreferencesKeys.TRASH_RETENTION_DAYS] ?: 14
            val characterLimit = preferences[PreferencesKeys.CHARACTER_LIMIT] ?: 1000
            val screenshotBlocked = preferences[PreferencesKeys.SCREENSHOT_BLOCKED] ?: false
            val hideInRecents = preferences[PreferencesKeys.HIDE_IN_RECENTS] ?: false
            val autoLockTimeoutSeconds = preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT_SECONDS] ?: 0
            val backupFolderUri = preferences[PreferencesKeys.BACKUP_FOLDER_URI]
            val backupSchedule = preferences[PreferencesKeys.BACKUP_SCHEDULE] ?: "OFF"

            UserPreferences(
                themeMode = themeMode,
                dynamicColor = dynamicColor,
                accentChoice = accentChoice,
                viewMode = viewMode,
                sortOption = sortOption,
                sortAscending = sortAscending,
                hapticsEnabled = hapticsEnabled,
                onboardingCompleted = onboardingCompleted,
                biometricEnabled = biometricEnabled,
                pinHash = pinHash,
                trashRetentionDays = trashRetentionDays,
                characterLimit = characterLimit,
                screenshotBlocked = screenshotBlocked,
                hideInRecents = hideInRecents,
                autoLockTimeoutSeconds = autoLockTimeoutSeconds,
                backupFolderUri = backupFolderUri,
                backupSchedule = backupSchedule
            )
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setAccentChoice(accent: AccentColorChoice) {
        context.dataStore.edit { it[PreferencesKeys.ACCENT_CHOICE] = accent.name }
    }

    override suspend fun setViewMode(mode: ViewMode) {
        context.dataStore.edit { it[PreferencesKeys.VIEW_MODE] = mode.name }
    }

    override suspend fun setSortOption(option: SortOption, ascending: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.SORT_OPTION] = option.name
            it[PreferencesKeys.SORT_ASCENDING] = ascending
        }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTICS_ENABLED] = enabled }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BIOMETRIC_ENABLED] = enabled }
    }

    override suspend fun setPinHash(hash: String?) {
        context.dataStore.edit {
            if (hash != null) it[PreferencesKeys.PIN_HASH] = hash else it.remove(PreferencesKeys.PIN_HASH)
        }
    }

    override suspend fun setTrashRetentionDays(days: Int) {
        context.dataStore.edit { it[PreferencesKeys.TRASH_RETENTION_DAYS] = days }
    }

    override suspend fun setCharacterLimit(limit: Int) {
        context.dataStore.edit { it[PreferencesKeys.CHARACTER_LIMIT] = limit }
    }

    override suspend fun setScreenshotBlocked(blocked: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SCREENSHOT_BLOCKED] = blocked }
    }

    override suspend fun setHideInRecents(hide: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HIDE_IN_RECENTS] = hide }
    }

    override suspend fun setAutoLockTimeoutSeconds(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_LOCK_TIMEOUT_SECONDS] = seconds }
    }

    override suspend fun setBackupFolderUri(uri: String?) {
        context.dataStore.edit {
            if (uri != null) it[PreferencesKeys.BACKUP_FOLDER_URI] = uri else it.remove(PreferencesKeys.BACKUP_FOLDER_URI)
        }
    }

    override suspend fun setBackupSchedule(schedule: String) {
        context.dataStore.edit { it[PreferencesKeys.BACKUP_SCHEDULE] = schedule }
    }
}
