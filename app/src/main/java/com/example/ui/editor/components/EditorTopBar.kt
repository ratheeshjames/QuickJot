package com.example.ui.editor.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
    isSaving: Boolean,
    saveStatus: String,
    canUndo: Boolean,
    canRedo: Boolean,
    isPinned: Boolean,
    isLocked: Boolean,
    isReadOnly: Boolean,
    hasReminder: Boolean,
    onBackClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onPinClick: () -> Unit,
    onReminderClick: () -> Unit,
    onColorClick: () -> Unit,
    onFindReplaceClick: () -> Unit,
    onVersionHistoryClick: () -> Unit,
    onToggleReadOnlyClick: () -> Unit,
    onToggleLockClick: () -> Unit,
    onFolderClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    haptic: HapticFeedbackHelper,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isSaving) "Saving…" else saveStatus.ifBlank { "QuickJot" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    haptic.tick()
                    onBackClick()
                },
                modifier = Modifier.testTag("editor_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
        actions = {
            // Undo
            IconButton(
                onClick = {
                    haptic.tick()
                    onUndoClick()
                },
                enabled = canUndo,
                modifier = Modifier.testTag("editor_undo_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.undo))
            }

            // Redo
            IconButton(
                onClick = {
                    haptic.tick()
                    onRedoClick()
                },
                enabled = canRedo,
                modifier = Modifier.testTag("editor_redo_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = stringResource(R.string.redo))
            }

            // Pin toggle
            IconButton(
                onClick = {
                    haptic.tick()
                    onPinClick()
                },
                modifier = Modifier.testTag("editor_pin_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = stringResource(R.string.cd_pin),
                    tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reminder
            IconButton(
                onClick = {
                    haptic.tick()
                    onReminderClick()
                },
                modifier = Modifier.testTag("editor_reminder_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = stringResource(R.string.cd_reminder),
                    tint = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Color picker
            IconButton(
                onClick = {
                    haptic.tick()
                    onColorClick()
                },
                modifier = Modifier.testTag("editor_color_button")
            ) {
                Icon(Icons.Default.ColorLens, contentDescription = stringResource(R.string.cd_color))
            }

            // Overflow menu
            Box {
                IconButton(
                    onClick = {
                        haptic.tick()
                        showMenu = true
                    },
                    modifier = Modifier.testTag("editor_more_options")
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_more_options))
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.find_and_replace)) },
                        leadingIcon = { Icon(Icons.Default.FindReplace, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onFindReplaceClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.version_history)) },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onVersionHistoryClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isReadOnly) "Disable Read-Only" else "Read-Only Mode") },
                        leadingIcon = {
                            Icon(
                                if (isReadOnly) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleReadOnlyClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isLocked) "Unlock Note" else "Lock Note") },
                        leadingIcon = {
                            Icon(
                                if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleLockClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to Folder") },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onFolderClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.duplicate_note)) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onDuplicateClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    )
}
