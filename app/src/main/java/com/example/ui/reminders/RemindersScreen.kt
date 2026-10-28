package com.example.ui.reminders

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView
import com.example.ui.navigation.QuickJotRoute
import com.example.ui.theme.NoteColor
import com.example.ui.util.HapticFeedbackHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    haptic: HapticFeedbackHelper,
    onNavigate: (QuickJotRoute) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var itemToReschedule by remember { mutableStateOf<ReminderItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshPermission()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("reminders_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val totalReminders = uiState.overdue.size + uiState.today.size + uiState.upcoming.size + uiState.completed.size

            if (totalReminders == 0) {
                EmptyStateView(
                    icon = Icons.Default.Alarm,
                    title = "No reminders scheduled",
                    description = "Add reminders to your notes to receive notifications at exact times or recurring intervals",
                    actionLabel = "Create a Note",
                    onActionClick = {
                        onNavigate(QuickJotRoute.Editor())
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Exact Alarm Warning Banner (Android 12+)
                    if (!uiState.hasExactAlarmPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Exact Alarms Permission Required",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "To trigger reminders at the exact minute, enable alarm permission in Android settings.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(
                                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                                    Uri.parse("package:${context.packageName}")
                                                )
                                                context.startActivity(intent)
                                            }
                                        ) {
                                            Text("Enable in Settings")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section: Overdue
                    if (uiState.overdue.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Overdue", count = uiState.overdue.size, isWarning = true)
                        }
                        items(uiState.overdue, key = { it.reminder.id }) { item ->
                            ReminderItemCard(
                                item = item,
                                isOverdue = true,
                                haptic = haptic,
                                onOpenNote = { onNavigate(QuickJotRoute.Editor(noteId = it.note.note.id)) },
                                onMarkDone = { viewModel.markDone(it.note.note.id) },
                                onSnooze = { viewModel.snooze(it.note.note.id) },
                                onReschedule = { itemToReschedule = it },
                                onDelete = { viewModel.deleteReminder(it.note.note.id) }
                            )
                        }
                    }

                    // Section: Today
                    if (uiState.today.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Today", count = uiState.today.size)
                        }
                        items(uiState.today, key = { it.reminder.id }) { item ->
                            ReminderItemCard(
                                item = item,
                                isOverdue = false,
                                haptic = haptic,
                                onOpenNote = { onNavigate(QuickJotRoute.Editor(noteId = it.note.note.id)) },
                                onMarkDone = { viewModel.markDone(it.note.note.id) },
                                onSnooze = { viewModel.snooze(it.note.note.id) },
                                onReschedule = { itemToReschedule = it },
                                onDelete = { viewModel.deleteReminder(it.note.note.id) }
                            )
                        }
                    }

                    // Section: Upcoming
                    if (uiState.upcoming.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Upcoming", count = uiState.upcoming.size)
                        }
                        items(uiState.upcoming, key = { it.reminder.id }) { item ->
                            ReminderItemCard(
                                item = item,
                                isOverdue = false,
                                haptic = haptic,
                                onOpenNote = { onNavigate(QuickJotRoute.Editor(noteId = it.note.note.id)) },
                                onMarkDone = { viewModel.markDone(it.note.note.id) },
                                onSnooze = { viewModel.snooze(it.note.note.id) },
                                onReschedule = { itemToReschedule = it },
                                onDelete = { viewModel.deleteReminder(it.note.note.id) }
                            )
                        }
                    }

                    // Section: Completed
                    if (uiState.completed.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Completed", count = uiState.completed.size)
                        }
                        items(uiState.completed, key = { it.reminder.id }) { item ->
                            ReminderItemCard(
                                item = item,
                                isOverdue = false,
                                haptic = haptic,
                                onOpenNote = { onNavigate(QuickJotRoute.Editor(noteId = it.note.note.id)) },
                                onMarkDone = { viewModel.markDone(it.note.note.id) },
                                onSnooze = { viewModel.snooze(it.note.note.id) },
                                onReschedule = { itemToReschedule = it },
                                onDelete = { viewModel.deleteReminder(it.note.note.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    // Reschedule Dialog
    itemToReschedule?.let { item ->
        RescheduleReminderDialog(
            item = item,
            onDismiss = { itemToReschedule = null },
            onConfirm = { triggerMillis, repeatInterval, customDays ->
                viewModel.updateReminder(
                    noteId = item.note.note.id,
                    triggerMillis = triggerMillis,
                    repeatInterval = repeatInterval,
                    customDays = customDays
                )
                itemToReschedule = null
                haptic.performSuccess()
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, isWarning: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ReminderItemCard(
    item: ReminderItem,
    isOverdue: Boolean,
    haptic: HapticFeedbackHelper,
    onOpenNote: (ReminderItem) -> Unit,
    onMarkDone: (ReminderItem) -> Unit,
    onSnooze: (ReminderItem) -> Unit,
    onReschedule: (ReminderItem) -> Unit,
    onDelete: (ReminderItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val noteColor = NoteColor.fromId(item.note.note.colorId)
    val triggerTime = item.reminder.snoozeUntilEpochMillis ?: item.reminder.triggerTimeEpochMillis
    val formattedTime = formatReminderTime(triggerTime)
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performLight()
                onOpenNote(item)
            }
            .testTag("reminder_item_${item.reminder.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = noteColor.lightContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox to mark completed
            IconButton(
                onClick = {
                    haptic.performSuccess()
                    onMarkDone(item)
                },
                modifier = Modifier.size(36.dp).testTag("reminder_check_${item.reminder.id}")
            ) {
                Icon(
                    imageVector = if (item.reminder.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Mark done",
                    tint = if (item.reminder.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.note.note.title.ifBlank { "Untitled Note" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (item.note.note.body.isNotBlank()) {
                    Text(
                        text = item.note.note.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.reminder.snoozeUntilEpochMillis != null) Icons.Default.Snooze else Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    if (item.reminder.repeatInterval != "NONE") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = when (item.reminder.repeatInterval) {
                                "DAILY" -> "Daily"
                                "WEEKLY" -> "Weekly"
                                "MONTHLY" -> "Monthly"
                                "CUSTOM" -> "Every ${item.reminder.customIntervalDays}d"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Snooze 15m
            if (!item.reminder.isCompleted) {
                IconButton(
                    onClick = {
                        haptic.performLight()
                        onSnooze(item)
                    },
                    modifier = Modifier.size(36.dp).testTag("reminder_snooze_${item.reminder.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Snooze 15m",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Options menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Reschedule") },
                        leadingIcon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onReschedule(item)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Reminder", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RescheduleReminderDialog(
    item: ReminderItem,
    onDismiss: () -> Unit,
    onConfirm: (triggerMillis: Long, repeatInterval: String, customDays: Int) -> Unit
) {
    val cal = Calendar.getInstance()
    var selectedOffsetHours by remember { mutableIntStateOf(1) }
    var repeatInterval by remember { mutableStateOf(item.reminder.repeatInterval) }
    var customDays by remember { mutableIntStateOf(item.reminder.customIntervalDays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.AlarmOn, contentDescription = null) },
        title = { Text("Reschedule Reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetChip(
                        label = "+1 Hour",
                        isSelected = selectedOffsetHours == 1,
                        onClick = { selectedOffsetHours = 1 }
                    )
                    PresetChip(
                        label = "+3 Hours",
                        isSelected = selectedOffsetHours == 3,
                        onClick = { selectedOffsetHours = 3 }
                    )
                    PresetChip(
                        label = "Tomorrow",
                        isSelected = selectedOffsetHours == 24,
                        onClick = { selectedOffsetHours = 24 }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Repeat Interval:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val intervals = listOf("NONE" to "Does not repeat", "DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly")
                intervals.forEach { (key, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { repeatInterval = key }
                    ) {
                        RadioButton(
                            selected = repeatInterval == key,
                            onClick = { repeatInterval = key }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val targetMillis = System.currentTimeMillis() + (selectedOffsetHours * 60L * 60L * 1000L)
                    onConfirm(targetMillis, repeatInterval, customDays)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PresetChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatReminderTime(epochMillis: Long): String {
    val date = Date(epochMillis)
    val now = Calendar.getInstance()
    val remCal = Calendar.getInstance().apply { time = date }

    val isToday = now.get(Calendar.YEAR) == remCal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == remCal.get(Calendar.DAY_OF_YEAR)

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    return if (isToday) {
        "Today at ${timeFormat.format(date)}"
    } else {
        dateFormat.format(date)
    }
}
