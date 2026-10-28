package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.QuickJotApplication
import com.example.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val app = context.applicationContext as? QuickJotApplication ?: return
            val repository = app.container.notesRepository
            val scheduler = ReminderScheduler(context)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeReminders = repository.activeRemindersFlow.first()
                    val now = System.currentTimeMillis()

                    activeReminders.forEach { reminder ->
                        val targetTime = reminder.snoozeUntilEpochMillis ?: reminder.triggerTimeEpochMillis
                        if (targetTime > now && !reminder.isCompleted) {
                            val note = repository.getNote(reminder.noteId)
                            if (note != null && !note.isTrash) {
                                scheduler.schedule(
                                    reminder = reminder,
                                    noteTitle = note.title,
                                    noteSnippet = note.body.take(100)
                                )
                            }
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
