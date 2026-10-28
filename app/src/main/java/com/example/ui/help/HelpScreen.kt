package com.example.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    haptic: HapticFeedbackHelper,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Tips", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.click()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("help_back_button")
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
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HelpCard(
                    icon = Icons.Default.Swipe,
                    title = "Gestures & Shortcuts",
                    tips = listOf(
                        "Swipe Left on any note card to quickly move it to Trash.",
                        "Swipe Right on any note card to toggle Pin / Unpin.",
                        "Long-press any note card to trigger multi-select mode for bulk color, pin, move, or delete.",
                        "Reorder checklist items easily by dragging the reorder handles in the editor."
                    )
                )
            }

            item {
                HelpCard(
                    icon = Icons.Default.FormatBold,
                    title = "Editor & Formatting",
                    tips = listOf(
                        "Use the horizontal formatting toolbar to format text as bold, italic, underline, or strikethrough.",
                        "Add checklists, bullet lists, or numbered lists with automatic continuation on Enter.",
                        "Find and replace text directly within the note editor.",
                        "Automatic drafts & version history: revert any note to an earlier snapshot with 1 tap."
                    )
                )
            }

            item {
                HelpCard(
                    icon = Icons.Default.Alarm,
                    title = "Reminders & Notifications",
                    tips = listOf(
                        "Schedule exact one-time or recurring reminders (daily, weekly, monthly).",
                        "Snooze alarms for 15 minutes or mark them as done directly from the notification shade.",
                        "Reminders survive device restarts automatically via Android boot receivers."
                    )
                )
            }

            item {
                HelpCard(
                    icon = Icons.Default.Lock,
                    title = "Security & Privacy",
                    tips = listOf(
                        "100% Offline: QuickJot contains zero network permissions and makes zero network calls.",
                        "SQLCipher 256-bit AES database encryption protected by the Android Keystore.",
                        "Secure app launch with 4-digit PIN and Biometric Prompt (fingerprint or face).",
                        "Per-note lock: lock confidential notes individually with biometric authentication.",
                        "Prevent screenshots and hide card previews in Android Recents screen."
                    )
                )
            }

            item {
                HelpCard(
                    icon = Icons.Default.Widgets,
                    title = "Widgets & Quick Capture",
                    tips = listOf(
                        "Glance Home Screen Widget: place QuickJot on your home screen for 1-tap quick jotting.",
                        "Quick Settings Tile: add the QuickJot tile to your notification panel for capture anywhere.",
                        "Launcher Shortcuts: long-press the QuickJot app icon on your home screen for quick actions.",
                        "System Share Target: share text or links from browser or other apps directly into QuickJot."
                    )
                )
            }
        }
    }
}

@Composable
private fun HelpCard(
    icon: ImageVector,
    title: String,
    tips: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
