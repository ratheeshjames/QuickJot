package com.example.ui.reminders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.models.NoteWithDetails
import com.example.domain.repository.NotesRepository
import com.example.reminders.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class ReminderItem(
    val reminder: ReminderEntity,
    val note: NoteWithDetails
)

data class RemindersUiState(
    val overdue: List<ReminderItem> = emptyList(),
    val today: List<ReminderItem> = emptyList(),
    val upcoming: List<ReminderItem> = emptyList(),
    val completed: List<ReminderItem> = emptyList(),
    val hasExactAlarmPermission: Boolean = true,
    val isLoading: Boolean = false
)

class RemindersViewModel(
    private val notesRepository: NotesRepository,
    private val context: Context
) : ViewModel() {

    private val scheduler = ReminderScheduler(context)
    private val permissionCheckFlow = MutableStateFlow(scheduler.canScheduleExactAlarms())

    val uiState: StateFlow<RemindersUiState> = combine(
        notesRepository.activeRemindersFlow,
        notesRepository.activeNotesFlow,
        permissionCheckFlow
    ) { reminders, notes, hasPermission ->
        val notesMap = notes.associateBy { it.note.id }

        val items = reminders.mapNotNull { reminder ->
            val note = notesMap[reminder.noteId]
            if (note != null) ReminderItem(reminder, note) else null
        }

        val now = System.currentTimeMillis()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfToday = startOfToday + (24L * 60L * 60L * 1000L)

        val completed = items.filter { it.reminder.isCompleted }
        val active = items.filter { !it.reminder.isCompleted }

        val overdue = active.filter {
            val trigger = it.reminder.snoozeUntilEpochMillis ?: it.reminder.triggerTimeEpochMillis
            trigger < now
        }

        val today = active.filter {
            val trigger = it.reminder.snoozeUntilEpochMillis ?: it.reminder.triggerTimeEpochMillis
            trigger in now..endOfToday
        }

        val upcoming = active.filter {
            val trigger = it.reminder.snoozeUntilEpochMillis ?: it.reminder.triggerTimeEpochMillis
            trigger > endOfToday
        }

        RemindersUiState(
            overdue = overdue,
            today = today,
            upcoming = upcoming,
            completed = completed,
            hasExactAlarmPermission = hasPermission,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemindersUiState(isLoading = true)
    )

    fun refreshPermission() {
        permissionCheckFlow.value = scheduler.canScheduleExactAlarms()
    }

    fun markDone(noteId: Long) {
        viewModelScope.launch {
            notesRepository.markReminderCompleted(noteId)
            scheduler.cancel(noteId)
        }
    }

    fun deleteReminder(noteId: Long) {
        viewModelScope.launch {
            notesRepository.deleteReminder(noteId)
            scheduler.cancel(noteId)
        }
    }

    fun snooze(noteId: Long, minutes: Int = 15) {
        viewModelScope.launch {
            val snoozeTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
            notesRepository.snoozeReminder(noteId, snoozeTime)
            val note = notesRepository.getNote(noteId)
            if (note != null) {
                val reminder = ReminderEntity(
                    noteId = noteId,
                    triggerTimeEpochMillis = snoozeTime,
                    snoozeUntilEpochMillis = snoozeTime
                )
                scheduler.schedule(reminder, note.title, note.body.take(100))
            }
        }
    }

    fun updateReminder(
        noteId: Long,
        triggerMillis: Long,
        repeatInterval: String,
        customDays: Int
    ) {
        viewModelScope.launch {
            val reminder = ReminderEntity(
                noteId = noteId,
                triggerTimeEpochMillis = triggerMillis,
                repeatInterval = repeatInterval,
                customIntervalDays = customDays,
                isCompleted = false,
                snoozeUntilEpochMillis = null
            )
            notesRepository.setReminder(reminder)
            val note = notesRepository.getNote(noteId)
            if (note != null) {
                scheduler.schedule(reminder, note.title, note.body.take(100))
            }
        }
    }
}
