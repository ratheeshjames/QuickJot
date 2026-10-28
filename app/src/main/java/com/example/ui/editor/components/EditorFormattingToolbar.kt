package com.example.ui.editor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.editor.EditorTextSize
import com.example.ui.util.HapticFeedbackHelper

@Composable
fun EditorFormattingToolbar(
    charCount: Int,
    characterLimit: Int,
    wordCount: Int,
    currentTextSize: EditorTextSize,
    onFormatClick: (String) -> Unit,
    onToggleTextSize: () -> Unit,
    haptic: HapticFeedbackHelper,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Stats Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "$charCount/$characterLimit • $wordCount words",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (charCount >= characterLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Bold
            IconButton(
                onClick = {
                    haptic.tick()
                    onFormatClick("BOLD")
                },
                modifier = Modifier.size(36.dp).testTag("format_bold")
            ) {
                Icon(Icons.Default.FormatBold, contentDescription = stringResource(R.string.format_bold), modifier = Modifier.size(20.dp))
            }

            // Italic
            IconButton(
                onClick = {
                    haptic.tick()
                    onFormatClick("ITALIC")
                },
                modifier = Modifier.size(36.dp).testTag("format_italic")
            ) {
                Icon(Icons.Default.FormatItalic, contentDescription = stringResource(R.string.format_italic), modifier = Modifier.size(20.dp))
            }

            // Underline
            IconButton(
                onClick = {
                    haptic.tick()
                    onFormatClick("UNDERLINE")
                },
                modifier = Modifier.size(36.dp).testTag("format_underline")
            ) {
                Icon(Icons.Default.FormatUnderlined, contentDescription = stringResource(R.string.format_underline), modifier = Modifier.size(20.dp))
            }

            // Strikethrough
            IconButton(
                onClick = {
                    haptic.tick()
                    onFormatClick("STRIKE")
                },
                modifier = Modifier.size(36.dp).testTag("format_strikethrough")
            ) {
                Icon(Icons.Default.FormatStrikethrough, contentDescription = stringResource(R.string.format_strikethrough), modifier = Modifier.size(20.dp))
            }

            // Bullet List
            IconButton(
                onClick = {
                    haptic.tick()
                    onFormatClick("BULLET")
                },
                modifier = Modifier.size(36.dp).testTag("format_bullet")
            ) {
                Icon(Icons.Default.FormatListBulleted, contentDescription = stringResource(R.string.format_bullet), modifier = Modifier.size(20.dp))
            }

            // Numbered List
            IconButton(
                onClick = {
                    haptic.tick()
                    onFormatClick("NUMBER")
                },
                modifier = Modifier.size(36.dp).testTag("format_number")
            ) {
                Icon(Icons.Default.FormatListNumbered, contentDescription = stringResource(R.string.format_number), modifier = Modifier.size(20.dp))
            }

            // Checklist
            IconButton(
                onClick = {
                    haptic.tick()
                    onFormatClick("CHECK")
                },
                modifier = Modifier.size(36.dp).testTag("format_checklist")
            ) {
                Icon(Icons.Default.CheckBox, contentDescription = stringResource(R.string.format_check), modifier = Modifier.size(20.dp))
            }

            // Text Size Toggle
            IconButton(
                onClick = {
                    haptic.tick()
                    onToggleTextSize()
                },
                modifier = Modifier.size(36.dp).testTag("format_text_size")
            ) {
                Icon(Icons.Default.FormatSize, contentDescription = "Text Size", modifier = Modifier.size(20.dp))
            }
        }
    }
}
