package com.example.data.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.QuickJotApplication
import kotlinx.coroutines.flow.first

class TrashPurgeWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as? QuickJotApplication ?: return Result.failure()
            val container = app.container
            val prefs = container.userPreferencesRepository.preferencesFlow.first()
            val purgedCount = container.notesRepository.purgeOldTrash(prefs.trashRetentionDays)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "quickjot_trash_purge_work"
    }
}
