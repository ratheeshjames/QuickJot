package com.example.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.util.HapticFeedbackHelper

@Composable
fun ExpressiveFabMenu(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNewNote: () -> Unit,
    onNewChecklist: () -> Unit,
    onVoiceNote: () -> Unit,
    onClipboardNote: () -> Unit,
    haptic: HapticFeedbackHelper,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "FabRotation"
    )

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Expanded menu items
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut() + scaleOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FabMenuItem(
                    icon = Icons.Default.ContentPaste,
                    label = stringResource(R.string.action_clipboard_note),
                    onClick = {
                        haptic.tick()
                        onClipboardNote()
                    },
                    testTag = "fab_clipboard"
                )

                FabMenuItem(
                    icon = Icons.Default.Mic,
                    label = stringResource(R.string.action_voice_note),
                    onClick = {
                        haptic.tick()
                        onVoiceNote()
                    },
                    testTag = "fab_voice"
                )

                FabMenuItem(
                    icon = Icons.Default.CheckBox,
                    label = stringResource(R.string.action_new_checklist),
                    onClick = {
                        haptic.tick()
                        onNewChecklist()
                    },
                    testTag = "fab_checklist"
                )

                FabMenuItem(
                    icon = Icons.Default.Edit,
                    label = stringResource(R.string.action_new_note),
                    onClick = {
                        haptic.tick()
                        onNewNote()
                    },
                    testTag = "fab_text_note"
                )
            }
        }

        // Main FAB button
        FloatingActionButton(
            onClick = {
                haptic.click()
                onToggleExpand()
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .size(64.dp)
                .testTag("fab_main_menu")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.cd_create_note),
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        SmallFloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.testTag(testTag)
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
    }
}
