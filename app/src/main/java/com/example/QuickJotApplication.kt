package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer

class QuickJotApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        createNotificationChannels()
        scheduleBackgroundWorkers()
    }

    private fun scheduleBackgroundWorkers() {
        try {
            val workManager = androidx.work.WorkManager.getInstance(this)

            // Daily Trash Purge Worker (7, 14, or 30 days retention policy)
            val purgeRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.data.backup.TrashPurgeWorker>(
                1, java.util.concurrent.TimeUnit.DAYS
            ).build()
            workManager.enqueueUniquePeriodicWork(
                com.example.data.backup.TrashPurgeWorker.WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                purgeRequest
            )

            // Periodic Backup Worker
            val backupRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.data.backup.ScheduledBackupWorker>(
                1, java.util.concurrent.TimeUnit.DAYS
            ).build()
            workManager.enqueueUniquePeriodicWork(
                com.example.data.backup.ScheduledBackupWorker.WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                backupRequest
            )
        } catch (_: Exception) {
            // WorkManager may not be initialized in unit test or mock environments
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.nav_reminders)
            val descriptionText = getString(R.string.permission_alarm_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(REMINDER_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "quickjot_reminders_channel"
    }
}
