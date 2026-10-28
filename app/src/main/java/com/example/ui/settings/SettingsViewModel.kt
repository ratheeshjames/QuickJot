package com.example.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.backup.BackupManager
import com.example.data.backup.RestoreResult
import com.example.data.backup.ScheduledBackupWorker
import com.example.data.backup.TrashPurgeWorker
import com.example.data.security.SecurityManager
import com.example.domain.repository.NotesRepository
import com.example.domain.repository.ThemeMode
import com.example.domain.repository.UserPreferences
import com.example.domain.repository.UserPreferencesRepository
import com.example.domain.repository.ViewMode
import com.example.ui.theme.AccentColorChoice
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val hasPin: Boolean = false,
    val isExporting: Boolean = false,
    val isRestoring: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notesRepository: NotesRepository,
    private val securityManager: SecurityManager,
    private val backupManager: BackupManager,
    private val appContext: Context
) : ViewModel() {

    val preferencesState: StateFlow<UserPreferences> = userPreferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDynamicColor(enabled)
        }
    }

    fun setAccentChoice(accent: AccentColorChoice) {
        viewModelScope.launch {
            userPreferencesRepository.setAccentChoice(accent)
        }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            userPreferencesRepository.setViewMode(mode)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setHapticsEnabled(enabled)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBiometricEnabled(enabled)
        }
    }

    fun savePin(pin: String) {
        viewModelScope.launch {
            val hash = SecurityManager.hashPin(pin)
            userPreferencesRepository.setPinHash(hash)
        }
    }

    fun removePin() {
        viewModelScope.launch {
            userPreferencesRepository.setPinHash(null)
            userPreferencesRepository.setBiometricEnabled(false)
        }
    }

    fun setTrashRetentionDays(days: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setTrashRetentionDays(days)
            // Schedule daily purge worker
            val purgeRequest = PeriodicWorkRequestBuilder<TrashPurgeWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                TrashPurgeWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                purgeRequest
            )
        }
    }

    fun setCharacterLimit(limit: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setCharacterLimit(limit)
        }
    }

    fun setScreenshotBlocked(blocked: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setScreenshotBlocked(blocked)
        }
    }

    fun setHideInRecents(hide: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setHideInRecents(hide)
        }
    }

    fun setAutoLockTimeoutSeconds(seconds: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setAutoLockTimeoutSeconds(seconds)
        }
    }

    fun setBackupFolderUri(uri: String?) {
        viewModelScope.launch {
            userPreferencesRepository.setBackupFolderUri(uri)
            updateScheduledBackupWork(preferencesState.value.backupSchedule, uri)
        }
    }

    fun setBackupSchedule(schedule: String) {
        viewModelScope.launch {
            userPreferencesRepository.setBackupSchedule(schedule)
            updateScheduledBackupWork(schedule, preferencesState.value.backupFolderUri)
        }
    }

    private fun updateScheduledBackupWork(schedule: String, folderUri: String?) {
        val workManager = WorkManager.getInstance(appContext)
        if (schedule == "OFF" || folderUri == null) {
            workManager.cancelUniqueWork(ScheduledBackupWorker.WORK_NAME)
        } else {
            val intervalDays = if (schedule == "DAILY") 1L else 7L
            val request = PeriodicWorkRequestBuilder<ScheduledBackupWorker>(intervalDays, TimeUnit.DAYS).build()
            workManager.enqueueUniquePeriodicWork(
                ScheduledBackupWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    fun emptyTrash(onDone: () -> Unit) {
        viewModelScope.launch {
            notesRepository.emptyTrash()
            onDone()
        }
    }

    fun exportBackup(
        context: Context,
        uri: Uri,
        password: String? = null,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = backupManager.exportBackup(context, uri, password)
            onComplete(result)
        }
    }

    fun restoreBackup(
        context: Context,
        uri: Uri,
        password: String? = null,
        onComplete: (RestoreResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = backupManager.restoreBackup(context, uri, password)
            onComplete(result)
        }
    }
}
