package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ExpressiveColorPicker
import com.example.ui.editor.components.ChecklistEditorView
import com.example.ui.editor.components.EditorFormattingToolbar
import com.example.ui.editor.components.EditorTopBar
import com.example.ui.editor.components.FindAndReplaceBar
import com.example.ui.editor.components.VersionHistoryDialog
import com.example.ui.theme.NoteColor
import com.example.ui.util.HapticFeedbackHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    haptic: HapticFeedbackHelper,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val currentNoteColor = NoteColor.fromId(uiState.colorId)
    val containerBg = currentNoteColor.container(isDark)
    val contentTextColor = currentNoteColor.onContainer(isDark)

    var showColorDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempReminderCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    // Intercept system Back button to save and exit smoothly
    BackHandler {
        haptic.tick()
        viewModel.onExit(onNavigateBack)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(containerBg),
        containerColor = containerBg,
        topBar = {
            EditorTopBar(
                isSaving = uiState.isSaving,
                saveStatus = uiState.saveStatusMessage,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                isPinned = uiState.isPinned,
                isLocked = uiState.isLocked,
                isReadOnly = uiState.isReadOnly,
                hasReminder = uiState.reminder != null && !uiState.reminder!!.isCompleted,
                onBackClick = {
                    viewModel.onExit(onNavigateBack)
                },
                onUndoClick = { viewModel.undo() },
                onRedoClick = { viewModel.redo() },
                onPinClick = { viewModel.togglePin() },
                onReminderClick = { showReminderDialog = true },
                onColorClick = { showColorDialog = true },
                onFindReplaceClick = { viewModel.toggleFindReplace() },
                onVersionHistoryClick = { viewModel.showVersionHistory(true) },
                onToggleReadOnlyClick = { viewModel.toggleReadOnly() },
                onToggleLockClick = { viewModel.toggleLock() },
                onFolderClick = { showFolderDialog = true },
                onDuplicateClick = {
                    viewModel.duplicateCurrentNote {
                        haptic.click()
                        onNavigateBack()
                    }
                },
                onDeleteClick = {
                    viewModel.deleteNote {
                        haptic.heavyClick()
                        onNavigateBack()
                    }
                },
                haptic = haptic
            )
        },
        bottomBar = {
            Column(modifier = Modifier.imePadding()) {
                AnimatedVisibility(visible = uiState.showFindReplace) {
                    FindAndReplaceBar(
                        findQuery = uiState.findQuery,
                        replaceQuery = uiState.replaceQuery,
                        matchCount = uiState.findMatchesCount,
                        onFindQueryChange = { viewModel.updateFindQuery(it) },
                        onReplaceQueryChange = { viewModel.updateReplaceQuery(it) },
                        onReplaceOne = { viewModel.replaceOne() },
                        onReplaceAll = { viewModel.replaceAll() },
                        onClose = { viewModel.toggleFindReplace(false) },
                        haptic = haptic
                    )
                }

                if (!uiState.isReadOnly) {
                    EditorFormattingToolbar(
                        charCount = uiState.charCount,
                        characterLimit = uiState.characterLimit,
                        wordCount = uiState.wordCount,
                        currentTextSize = uiState.textSize,
                        onFormatClick = { tag -> viewModel.applyFormatting(tag) },
                        onToggleTextSize = {
                            val next = when (uiState.textSize) {
                                EditorTextSize.SMALL -> EditorTextSize.MEDIUM
                                EditorTextSize.MEDIUM -> EditorTextSize.LARGE
                                EditorTextSize.LARGE -> EditorTextSize.SMALL
                            }
                            viewModel.setTextSize(next)
                        },
                        haptic = haptic
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Badges row: Folder, Reminder, Tags
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Folder badge
                val currentFolder = uiState.allFolders.firstOrNull { it.id == uiState.folderId }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = contentTextColor.copy(alpha = 0.12f),
                    modifier = Modifier.clickable { showFolderDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = contentTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentFolder?.name ?: "No Folder",
                            style = MaterialTheme.typography.labelMedium,
                            color = contentTextColor
                        )
                    }
                }

                // Reminder badge
                if (uiState.reminder != null && !uiState.reminder!!.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { showReminderDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formatReminder(uiState.reminder!!.triggerTimeEpochMillis),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Active Tags
                uiState.allTags.filter { uiState.selectedTagIds.contains(it.id) }.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = contentTextColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${tag.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = contentTextColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove tag",
                                tint = contentTextColor.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.toggleTag(tag.id) }
                            )
                        }
                    }
                }

                // Add Tag Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = contentTextColor.copy(alpha = 0.08f),
                    modifier = Modifier.clickable { showTagDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = contentTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tag",
                            style = MaterialTheme.typography.labelMedium,
                            color = contentTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title Field
            BasicTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                readOnly = uiState.isReadOnly,
                textStyle = TextStyle(
                    color = contentTextColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox = { innerTextField ->
                    if (uiState.title.isEmpty()) {
                        Text(
                            text = stringResource(R.string.note_title_hint),
                            color = contentTextColor.copy(alpha = 0.4f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body or Checklist Content
            if (uiState.noteType == "checklist" || uiState.checklistItems.isNotEmpty()) {
                ChecklistEditorView(
                    items = uiState.checklistItems,
                    isReadOnly = uiState.isReadOnly,
                    onItemTextChange = { idx, text -> viewModel.updateChecklistItem(idx, text) },
                    onItemDoneChange = { idx, done -> viewModel.toggleChecklistItem(idx, done) },
                    onAddItem = { afterIdx -> viewModel.addChecklistItem(afterIdx) },
                    onRemoveItem = { idx -> viewModel.removeChecklistItem(idx) },
                    haptic = haptic
                )
            } else {
                val bodyFontSize = (17 * uiState.textSize.scaleFactor).sp
                val bodyLineHeight = (26 * uiState.textSize.scaleFactor).sp

                BasicTextField(
                    value = uiState.body,
                    onValueChange = { viewModel.updateBody(it) },
                    readOnly = uiState.isReadOnly,
                    textStyle = TextStyle(
                        color = contentTextColor.copy(alpha = 0.9f),
                        fontSize = bodyFontSize,
                        lineHeight = bodyLineHeight,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    decorationBox = { innerTextField ->
                        if (uiState.body.isEmpty()) {
                            Text(
                                text = stringResource(R.string.note_body_hint),
                                color = contentTextColor.copy(alpha = 0.35f),
                                fontSize = bodyFontSize,
                                lineHeight = bodyLineHeight
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 250.dp)
                        .testTag("note_body_input")
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Color Picker Dialog
        if (showColorDialog) {
            AlertDialog(
                onDismissRequest = { showColorDialog = false },
                title = { Text("Note Color") },
                text = {
                    ExpressiveColorPicker(
                        selectedColorId = uiState.colorId,
                        onColorSelected = { colorId ->
                            haptic.click()
                            viewModel.setColor(colorId)
                            showColorDialog = false
                        }
                    )
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showColorDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Folder Picker Dialog
        if (showFolderDialog) {
            AlertDialog(
                onDismissRequest = { showFolderDialog = false },
                title = { Text("Select Folder") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.folderId == null,
                            onClick = {
                                viewModel.setFolder(null)
                                showFolderDialog = false
                            },
                            label = { Text("No Folder") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        uiState.allFolders.forEach { folder ->
                            FilterChip(
                                selected = uiState.folderId == folder.id,
                                onClick = {
                                    viewModel.setFolder(folder.id)
                                    showFolderDialog = false
                                },
                                label = { Text(folder.name) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showFolderDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Tag Selector Dialog
        if (showTagDialog) {
            AlertDialog(
                onDismissRequest = { showTagDialog = false },
                title = { Text("Manage Tags") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.allTags.forEach { tag ->
                                val isSelected = uiState.selectedTagIds.contains(tag.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.tick()
                                        viewModel.toggleTag(tag.id)
                                    },
                                    label = { Text("#${tag.name}") }
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newTagInput,
                                onValueChange = { newTagInput = it },
                                placeholder = { Text("New tag name…") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newTagInput.isNotBlank()) {
                                        haptic.click()
                                        viewModel.createAndAddTag(newTagInput)
                                        newTagInput = ""
                                    }
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text("Add")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTagDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }

        // Reminder Selection Dialog
        if (showReminderDialog) {
            AlertDialog(
                onDismissRequest = { showReminderDialog = false },
                title = { Text("Set Reminder") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                showReminderDialog = false
                                showDatePicker = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick Date & Time")
                        }

                        if (uiState.reminder != null) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.removeReminder()
                                    showReminderDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Remove Reminder", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showReminderDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Date Picker
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val cal = Calendar.getInstance().apply { timeInMillis = millis }
                                tempReminderCalendar.set(Calendar.YEAR, cal.get(Calendar.YEAR))
                                tempReminderCalendar.set(Calendar.MONTH, cal.get(Calendar.MONTH))
                                tempReminderCalendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
                            }
                            showDatePicker = false
                            showTimePicker = true
                        }
                    ) {
                        Text("Next: Pick Time")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Time Picker
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = tempReminderCalendar.get(Calendar.HOUR_OF_DAY),
                initialMinute = tempReminderCalendar.get(Calendar.MINUTE)
            )

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("Pick Reminder Time") },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = timePickerState)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            tempReminderCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            tempReminderCalendar.set(Calendar.MINUTE, timePickerState.minute)
                            tempReminderCalendar.set(Calendar.SECOND, 0)
                            viewModel.setReminder(tempReminderCalendar.timeInMillis)
                            showTimePicker = false
                        }
                    ) {
                        Text("Save Reminder")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Version History Dialog
        if (uiState.showVersionHistory) {
            VersionHistoryDialog(
                versions = uiState.versions,
                onRestoreVersion = { version ->
                    viewModel.restoreVersion(version)
                },
                onDismiss = { viewModel.showVersionHistory(false) },
                haptic = haptic
            )
        }
    }
}

private fun formatReminder(epochMillis: Long): String {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}
