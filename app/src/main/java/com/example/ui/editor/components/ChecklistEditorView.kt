package com.example.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.ChecklistItemEntity
import com.example.ui.util.HapticFeedbackHelper

@Composable
fun ChecklistEditorView(
    items: List<ChecklistItemEntity>,
    isReadOnly: Boolean,
    onItemTextChange: (Int, String) -> Unit,
    onItemDoneChange: (Int, Boolean) -> Unit,
    onAddItem: (afterIndex: Int?) -> Unit,
    onRemoveItem: (Int) -> Unit,
    haptic: HapticFeedbackHelper,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEachIndexed { index, item ->
            ChecklistRow(
                item = item,
                isReadOnly = isReadOnly,
                onTextChange = { onItemTextChange(index, it) },
                onDoneChange = {
                    haptic.tick()
                    onItemDoneChange(index, it)
                },
                onEnterPressed = { onAddItem(index) },
                onDelete = {
                    haptic.tick()
                    onRemoveItem(index)
                }
            )
        }

        if (!isReadOnly) {
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = {
                    haptic.tick()
                    onAddItem(null)
                },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.add_checklist_item))
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItemEntity,
    isReadOnly: Boolean,
    onTextChange: (String) -> Unit,
    onDoneChange: (Boolean) -> Unit,
    onEnterPressed: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isDone,
            onCheckedChange = { if (!isReadOnly) onDoneChange(it) },
            enabled = !isReadOnly,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            readOnly = isReadOnly,
            singleLine = true,
            textStyle = TextStyle(
                color = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onEnterPressed() }),
            decorationBox = { innerTextField ->
                if (item.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.checklist_item_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            },
            modifier = Modifier.weight(1f)
        )

        if (!isReadOnly) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
