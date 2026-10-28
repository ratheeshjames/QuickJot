package com.example.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.models.NoteWithDetails
import com.example.domain.repository.ViewMode
import com.example.ui.theme.NoteCardDefaultShape
import com.example.ui.theme.NoteCardSelectedShape
import com.example.ui.theme.NoteColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    noteWithDetails: NoteWithDetails,
    viewMode: ViewMode,
    isSelected: Boolean,
    isInSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val note = noteWithDetails.note
    val isDark = isSystemInDarkTheme()
    val noteColor = NoteColor.fromId(note.colorId)

    val containerColor = noteColor.container(isDark)
    val onContainerColor = noteColor.onContainer(isDark)
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else noteColor.border(isDark)

    // Expressive shape morph target: smooth rounded corners to asymmetrical folded card
    val currentShape = if (isSelected) NoteCardSelectedShape else NoteCardDefaultShape
    val elevation by animateDpAsState(targetValue = if (isSelected) 6.dp else 1.dp, label = "ElevationAnim")

    Card(
        shape = currentShape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = onContainerColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = borderColor,
                shape = currentShape
            )
            .clip(currentShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("note_card_${note.id}")
    ) {
        when (viewMode) {
            ViewMode.COMPACT -> {
                CompactNoteCardContent(
                    noteWithDetails = noteWithDetails,
                    isSelected = isSelected,
                    isInSelectionMode = isInSelectionMode,
                    textColor = onContainerColor
                )
            }
            ViewMode.GRID, ViewMode.LIST -> {
                StandardNoteCardContent(
                    noteWithDetails = noteWithDetails,
                    isGrid = viewMode == ViewMode.GRID,
                    isSelected = isSelected,
                    isInSelectionMode = isInSelectionMode,
                    textColor = onContainerColor
                )
            }
        }
    }
}

@Composable
private fun CompactNoteCardContent(
    noteWithDetails: NoteWithDetails,
    isSelected: Boolean,
    isInSelectionMode: Boolean,
    textColor: androidx.compose.ui.graphics.Color
) {
    val note = noteWithDetails.note
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isInSelectionMode) {
            SelectionCheckIndicator(isSelected = isSelected)
            Spacer(modifier = Modifier.width(12.dp))
        }

        if (note.isPinned) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            val titleText = if (note.title.isNotBlank()) note.title else note.body.take(40)
            Text(
                text = titleText.ifBlank { "Untitled Note" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (note.isLocked) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }

        if (noteWithDetails.reminder != null && !noteWithDetails.reminder.isCompleted) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StandardNoteCardContent(
    noteWithDetails: NoteWithDetails,
    isGrid: Boolean,
    isSelected: Boolean,
    isInSelectionMode: Boolean,
    textColor: androidx.compose.ui.graphics.Color
) {
    val note = noteWithDetails.note
    val maxBodyLines = if (isGrid) 6 else 8

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Top Header: Title + Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isInSelectionMode) {
                SelectionCheckIndicator(isSelected = isSelected)
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (note.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Body Content or Checklist Preview
        if (note.isLocked) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Locked Note",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        } else if (noteWithDetails.checklistItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            val previewItems = noteWithDetails.checklistItems.take(4)
            previewItems.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (item.isDone) Icons.Default.CheckCircle else Icons.Default.CheckBox,
                        contentDescription = null,
                        tint = if (item.isDone) textColor.copy(alpha = 0.5f) else textColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.isDone) textColor.copy(alpha = 0.5f) else textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (noteWithDetails.checklistItems.size > 4) {
                Text(
                    text = "+${noteWithDetails.checklistItems.size - 4} more items",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else if (note.body.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.body,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.85f),
                maxLines = maxBodyLines,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Badges: Tags, Folder, Reminder
        val hasChips = noteWithDetails.tags.isNotEmpty() ||
                noteWithDetails.folder != null ||
                (noteWithDetails.reminder != null && !noteWithDetails.reminder.isCompleted)

        if (hasChips) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Folder badge
                if (noteWithDetails.folder != null) {
                    MiniBadge(
                        icon = Icons.Default.Folder,
                        label = noteWithDetails.folder.name,
                        textColor = textColor
                    )
                }

                // Reminder badge
                if (noteWithDetails.reminder != null && !noteWithDetails.reminder.isCompleted) {
                    val timeStr = formatReminderTime(noteWithDetails.reminder.triggerTimeEpochMillis)
                    MiniBadge(
                        icon = Icons.Default.Alarm,
                        label = timeStr,
                        textColor = textColor
                    )
                }

                // Tag badges
                noteWithDetails.tags.take(3).forEach { tag ->
                    MiniBadge(
                        icon = null,
                        label = "#${tag.name}",
                        textColor = textColor
                    )
                }
            }
        }

        // Bottom Date
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = formatDate(note.modifiedTimestamp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun SelectionCheckIndicator(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MiniBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    label: String,
    textColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = textColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.85f),
                fontSize = 11.sp
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun formatReminderTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
