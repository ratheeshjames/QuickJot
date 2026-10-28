package com.example.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.entities.ReminderEntity
import com.example.receivers.ReminderReceiver
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun schedule(reminder: ReminderEntity, noteTitle: String, noteSnippet: String) {
        val triggerTime = reminder.snoozeUntilEpochMillis ?: reminder.triggerTimeEpochMillis

        // Don't schedule if it is already past and not snoozed
        if (triggerTime <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_REMINDER
            putExtra(ReminderReceiver.EXTRA_NOTE_ID, reminder.noteId)
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderReceiver.EXTRA_NOTE_TITLE, noteTitle)
            putExtra(ReminderReceiver.EXTRA_NOTE_SNIPPET, noteSnippet)
            putExtra(ReminderReceiver.EXTRA_REPEAT_INTERVAL, reminder.repeatInterval)
            putExtra(ReminderReceiver.EXTRA_CUSTOM_DAYS, reminder.customIntervalDays)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback to non-exact alarm if exact permission was denied
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancel(noteId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        fun calculateNextTrigger(currentTrigger: Long, repeatInterval: String, customDays: Int = 1): Long {
            val cal = Calendar.getInstance().apply { timeInMillis = currentTrigger }
            when (repeatInterval) {
                "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                "CUSTOM" -> cal.add(Calendar.DAY_OF_YEAR, if (customDays > 0) customDays else 1)
                else -> return 0L
            }
            return cal.timeInMillis
        }
    }
}
