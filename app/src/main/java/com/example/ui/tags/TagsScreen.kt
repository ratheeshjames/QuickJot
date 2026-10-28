package com.example.ui.tags

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.TagEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.navigation.QuickJotRoute
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    viewModel: TagsViewModel,
    haptic: HapticFeedbackHelper,
    onNavigateBack: () -> Unit,
    onNavigate: (QuickJotRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var tagToRename by remember { mutableStateOf<TagEntity?>(null) }
    var tagToDelete by remember { mutableStateOf<TagEntity?>(null) }
    var tagToMerge by remember { mutableStateOf<TagEntity?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            placeholder = { Text("Search tags...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tags_search_input")
                        )
                    } else {
                        Text("Tags", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSearchActive) {
                                isSearchActive = false
                                viewModel.onSearchQueryChanged("")
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("tags_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) viewModel.onSearchQueryChanged("")
                        },
                        modifier = Modifier.testTag("tags_toggle_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search tags"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performLight()
                    showCreateDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Tag") },
                modifier = Modifier.testTag("tags_fab_create")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.tags.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Tag,
                    title = if (uiState.searchQuery.isBlank()) "No tags yet" else "No matching tags",
                    description = if (uiState.searchQuery.isBlank()) {
                        "Tags help you group and filter notes across different folders"
                    } else {
                        "Try a different search query"
                    },
                    actionLabel = if (uiState.searchQuery.isBlank()) "Create First Tag" else null,
                    onActionClick = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.tags,
                        key = { it.tag.id }
                    ) { item ->
                        TagItemRow(
                            item = item,
                            haptic = haptic,
                            onOpenTag = { tag ->
                                onNavigate(QuickJotRoute.Search(initialTag = tag.name))
                            },
                            onRename = { tag -> tagToRename = tag },
                            onDelete = { tag -> tagToDelete = tag },
                            onMerge = { tag -> tagToMerge = tag }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Create Tag Dialog
    if (showCreateDialog) {
        TagInputDialog(
            title = "New Tag",
            initialName = "",
            confirmLabel = "Create",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createTag(name)
                showCreateDialog = false
                haptic.performSuccess()
            }
        )
    }

    // Rename Tag Dialog
    tagToRename?.let { tag ->
        TagInputDialog(
            title = "Rename Tag",
            initialName = tag.name,
            confirmLabel = "Rename",
            onDismiss = { tagToRename = null },
            onConfirm = { newName ->
                viewModel.renameTag(tag.id, newName)
                tagToRename = null
                haptic.performSuccess()
            }
        )
    }

    // Delete Tag Confirmation Dialog
    tagToDelete?.let { tag ->
        AlertDialog(
            onDismissRequest = { tagToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete '#${tag.name}'?") },
            text = {
                Text("This will remove the tag from all notes. The notes themselves will not be deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTag(tag.id)
                        tagToDelete = null
                        haptic.performWarning()
                    },
                    modifier = Modifier.testTag("confirm_delete_tag_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { tagToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Merge Tag Dialog
    tagToMerge?.let { sourceTag ->
        val candidateTargets = uiState.rawTags.filter { it.id != sourceTag.id }
        var selectedTargetId by remember { mutableLongStateOf(candidateTargets.firstOrNull()?.id ?: 0L) }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { tagToMerge = null },
            icon = { Icon(Icons.Default.CallMerge, contentDescription = null) },
            title = { Text("Merge '#${sourceTag.name}' into...") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("All notes currently tagged with '#${sourceTag.name}' will receive the destination tag, and '#${sourceTag.name}' will be removed.")
                    if (candidateTargets.isNotEmpty()) {
                        Box {
                            val targetName = candidateTargets.firstOrNull { it.id == selectedTargetId }?.name ?: ""
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("#$targetName", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                candidateTargets.forEach { target ->
                                    DropdownMenuItem(
                                        text = { Text("#${target.name}") },
                                        onClick = {
                                            selectedTargetId = target.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text("No other tags available to merge into.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedTargetId != 0L) {
                            viewModel.mergeTags(sourceTag.id, selectedTargetId)
                            tagToMerge = null
                            haptic.performSuccess()
                        }
                    },
                    enabled = candidateTargets.isNotEmpty() && selectedTargetId != 0L,
                    modifier = Modifier.testTag("confirm_merge_tag_button")
                ) {
                    Text("Merge")
                }
            },
            dismissButton = {
                TextButton(onClick = { tagToMerge = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TagItemRow(
    item: TagWithNoteCount,
    haptic: HapticFeedbackHelper,
    onOpenTag: (TagEntity) -> Unit,
    onRename: (TagEntity) -> Unit,
    onDelete: (TagEntity) -> Unit,
    onMerge: (TagEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performLight()
                onOpenTag(item.tag)
            }
            .testTag("tag_row_${item.tag.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${item.tag.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.noteCount} note${if (item.noteCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("tag_menu_${item.tag.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tag options"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open Notes") },
                        leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onOpenTag(item.tag)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename Tag") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRename(item.tag)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Merge into Another Tag") },
                        leadingIcon = { Icon(Icons.Default.CallMerge, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onMerge(item.tag)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Tag", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete(item.tag)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TagInputDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tag Name") },
                prefix = { Text("#") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tag_name_input")
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clean = name.trim().removePrefix("#")
                    if (clean.isNotBlank()) {
                        onConfirm(clean)
                    }
                },
                enabled = name.trim().removePrefix("#").isNotBlank(),
                modifier = Modifier.testTag("confirm_tag_button")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
