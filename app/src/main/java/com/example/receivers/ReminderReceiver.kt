package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.QuickJotApplication
import com.example.data.local.entities.ReminderEntity
import com.example.reminders.ReminderNotificationHelper
import com.example.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (noteId == -1L) return

        val app = context.applicationContext as? QuickJotApplication ?: return
        val container = app.container
        val repository = container.notesRepository
        val prefsRepository = container.userPreferencesRepository
        val notificationHelper = ReminderNotificationHelper(context)
        val scheduler = ReminderScheduler(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_TRIGGER_REMINDER -> {
                        val note = repository.getNote(noteId)
                        val prefs = prefsRepository.preferencesFlow.first()
                        val title = intent.getStringExtra(EXTRA_NOTE_TITLE) ?: note?.title ?: "Reminder"
                        val snippet = intent.getStringExtra(EXTRA_NOTE_SNIPPET) ?: note?.body?.take(100) ?: ""
                        val repeatInterval = intent.getStringExtra(EXTRA_REPEAT_INTERVAL) ?: "NONE"
                        val customDays = intent.getIntExtra(EXTRA_CUSTOM_DAYS, 1)

                        // If note is still active (not trashed or deleted)
                        if (note != null && !note.isTrash) {
                            val hideContent = prefs.biometricEnabled || prefs.pinHash != null || prefs.hideInRecents
                            notificationHelper.showReminderNotification(
                                noteId = noteId,
                                reminderId = reminderId,
                                title = title,
                                bodySnippet = snippet,
                                hideContent = hideContent
                            )

                            // Handle recurrence
                            if (repeatInterval != "NONE") {
                                val nextTrigger = ReminderScheduler.calculateNextTrigger(
                                    currentTrigger = System.currentTimeMillis(),
                                    repeatInterval = repeatInterval,
                                    customDays = customDays
                                )
                                if (nextTrigger > 0) {
                                    val updatedReminder = ReminderEntity(
                                        id = reminderId,
                                        noteId = noteId,
                                        triggerTimeEpochMillis = nextTrigger,
                                        repeatInterval = repeatInterval,
                                        customIntervalDays = customDays,
                                        isCompleted = false,
                                        snoozeUntilEpochMillis = null
                                    )
                                    repository.setReminder(updatedReminder)
                                    scheduler.schedule(updatedReminder, title, snippet)
                                }
                            }
                        }
                    }

                    ACTION_SNOOZE -> {
                        notificationHelper.dismissNotification(noteId)
                        val snoozeMinutes = 15
                        val snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
                        repository.snoozeReminder(noteId, snoozeTime)

                        val note = repository.getNote(noteId)
                        val title = note?.title ?: "Reminder"
                        val snippet = note?.body?.take(100) ?: ""
                        val existingReminder = repository.getReminderForNoteFlow(noteId).first()
                        if (existingReminder != null) {
                            val snoozedReminder = existingReminder.copy(snoozeUntilEpochMillis = snoozeTime)
                            scheduler.schedule(snoozedReminder, title, snippet)
                        }
                    }

                    ACTION_MARK_DONE -> {
                        notificationHelper.dismissNotification(noteId)
                        repository.markReminderCompleted(noteId)
                        scheduler.cancel(noteId)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.example.quickjot.TRIGGER_REMINDER"
        const val ACTION_SNOOZE = "com.example.quickjot.SNOOZE_REMINDER"
        const val ACTION_MARK_DONE = "com.example.quickjot.MARK_REMINDER_DONE"

        const val EXTRA_NOTE_ID = "EXTRA_NOTE_ID"
        const val EXTRA_REMINDER_ID = "EXTRA_REMINDER_ID"
        const val EXTRA_NOTE_TITLE = "EXTRA_NOTE_TITLE"
        const val EXTRA_NOTE_SNIPPET = "EXTRA_NOTE_SNIPPET"
        const val EXTRA_REPEAT_INTERVAL = "EXTRA_REPEAT_INTERVAL"
        const val EXTRA_CUSTOM_DAYS = "EXTRA_CUSTOM_DAYS"
    }
}
