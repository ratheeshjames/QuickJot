package com.example.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.receivers.ReminderReceiver

class ReminderNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Note Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts and reminders for your QuickJot notes"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(
        noteId: Long,
        reminderId: Long,
        title: String,
        bodySnippet: String,
        hideContent: Boolean
    ) {
        val displayTitle = if (hideContent) "QuickJot Reminder" else (title.ifBlank { "Note Reminder" })
        val displayBody = if (hideContent) "You have a scheduled note reminder." else bodySnippet.ifBlank { "Tap to view note" }

        // Content intent: Open note in editor
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_NOTE_ID, noteId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze intent: Snooze for 15 minutes
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SNOOZE
            putExtra(ReminderReceiver.EXTRA_NOTE_ID, noteId)
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderReceiver.EXTRA_NOTE_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_NOTE_SNIPPET, bodySnippet)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (noteId * 10 + 1).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Done intent: Mark reminder as completed
        val doneIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_MARK_DONE
            putExtra(ReminderReceiver.EXTRA_NOTE_ID, noteId)
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (noteId * 10 + 2).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(displayTitle)
            .setContentText(displayBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(displayBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(0, "Snooze (15m)", snoozePendingIntent)
            .addAction(0, "Mark Done", donePendingIntent)
            .build()

        notificationManager.notify(noteId.toInt(), notification)
    }

    fun dismissNotification(noteId: Long) {
        notificationManager.cancel(noteId.toInt())
    }

    companion object {
        const val CHANNEL_ID = "quickjot_reminders_channel"
        const val EXTRA_OPEN_NOTE_ID = "EXTRA_OPEN_NOTE_ID"
    }
}
