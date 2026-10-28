package com.example.data.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.QuickJotApplication
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduledBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as? QuickJotApplication ?: return Result.failure()
            val container = app.container
            val prefs = container.userPreferencesRepository.preferencesFlow.first()

            if (prefs.backupSchedule == "OFF" || prefs.backupFolderUri == null) {
                return Result.success()
            }

            val treeUri = Uri.parse(prefs.backupFolderUri)
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "quickjot_backup_$timestamp.qjbak"
            val newFileUri = DocumentsContract.createDocument(
                applicationContext.contentResolver,
                parentDocUri,
                "application/json",
                fileName
            ) ?: return Result.failure()

            val success = container.backupManager.exportBackup(
                context = applicationContext,
                uri = newFileUri
            )

            if (success) Result.success() else Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "quickjot_scheduled_backup_work"
    }
}
