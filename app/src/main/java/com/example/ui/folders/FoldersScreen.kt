package com.example.ui.folders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.FolderEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExpressiveColorPicker
import com.example.ui.navigation.QuickJotRoute
import com.example.ui.theme.NoteColor
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    viewModel: FoldersViewModel,
    haptic: HapticFeedbackHelper,
    onNavigateBack: () -> Unit,
    onNavigate: (QuickJotRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderEntity?>(null) }
    var parentFolderForNew by remember { mutableStateOf<FolderEntity?>(null) }
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
                            placeholder = { Text("Search folders...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("folders_search_input")
                        )
                    } else {
                        Text("Folders", fontWeight = FontWeight.Bold)
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
                        modifier = Modifier.testTag("folders_back_button")
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
                        modifier = Modifier.testTag("folders_toggle_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search folders"
                        )
                    }
                    IconButton(
                        onClick = {
                            parentFolderForNew = null
                            showCreateDialog = true
                        },
                        modifier = Modifier.testTag("folders_create_appbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "Create folder"
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
                    parentFolderForNew = null
                    showCreateDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Folder") },
                modifier = Modifier.testTag("folders_fab_create")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.folders.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.FolderOpen,
                    title = if (uiState.searchQuery.isBlank()) "No folders yet" else "No matching folders",
                    description = if (uiState.searchQuery.isBlank()) {
                        "Organize your thoughts into custom folders and subfolders"
                    } else {
                        "Try a different search term"
                    },
                    actionLabel = if (uiState.searchQuery.isBlank()) "Create First Folder" else null,
                    onActionClick = {
                        parentFolderForNew = null
                        showCreateDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.folders,
                        key = { it.folder.id }
                    ) { item ->
                        FolderTreeItem(
                            item = item,
                            haptic = haptic,
                            onOpenNotes = { folder ->
                                onNavigate(QuickJotRoute.Search(initialFolderId = folder.id))
                            },
                            onCreateNoteInFolder = { folder ->
                                onNavigate(QuickJotRoute.Editor(initialFolderId = folder.id))
                            },
                            onAddSubfolder = { folder ->
                                parentFolderForNew = folder
                                showCreateDialog = true
                            },
                            onEdit = { folder -> folderToEdit = folder },
                            onDelete = { folder -> folderToDelete = folder }
                        )
                    }

                    // Spacer for FAB clearance
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Create / Add Subfolder Dialog
    if (showCreateDialog) {
        FolderEditDialog(
            title = if (parentFolderForNew != null) "New Subfolder in '${parentFolderForNew?.name}'" else "New Folder",
            initialName = "",
            initialColorId = 0,
            availableParents = uiState.rawFolders,
            selectedParentId = parentFolderForNew?.id,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, parentId, colorId ->
                viewModel.createFolder(name, parentId, colorId)
                showCreateDialog = false
                haptic.performSuccess()
            }
        )
    }

    // Rename Folder Dialog
    folderToEdit?.let { folder ->
        FolderEditDialog(
            title = "Edit Folder",
            initialName = folder.name,
            initialColorId = folder.colorId,
            availableParents = uiState.rawFolders.filter { it.id != folder.id },
            selectedParentId = folder.parentId,
            onDismiss = { folderToEdit = null },
            onConfirm = { name, parentId, colorId ->
                viewModel.updateFolder(folder.copy(name = name, parentId = parentId, colorId = colorId))
                folderToEdit = null
                haptic.performSuccess()
            }
        )
    }

    // Delete Folder Confirmation Dialog
    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete '${folder.name}'?") },
            text = {
                Text("Notes inside this folder will not be deleted. They will remain accessible as unorganized notes.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFolder(folder.id)
                        folderToDelete = null
                        haptic.performWarning()
                    },
                    modifier = Modifier.testTag("confirm_delete_folder_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FolderTreeItem(
    item: FolderWithNoteCount,
    haptic: HapticFeedbackHelper,
    onOpenNotes: (FolderEntity) -> Unit,
    onCreateNoteInFolder: (FolderEntity) -> Unit,
    onAddSubfolder: (FolderEntity) -> Unit,
    onEdit: (FolderEntity) -> Unit,
    onDelete: (FolderEntity) -> Unit,
    indentationLevel: Int = 0,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }

    val noteColor = NoteColor.fromId(item.folder.colorId)
    val containerColor = noteColor.lightContainer.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = (indentationLevel * 20).dp)
            .animateContentSize()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performLight()
                    onOpenNotes(item.folder)
                }
                .testTag("folder_card_${item.folder.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Folder icon with custom color
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = noteColor.lightOnContainer.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = noteColor.lightOnContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.folder.name,
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

                // Subfolders expander toggle
                if (item.subfolders.isNotEmpty()) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }

                // Quick Note Creation in folder
                IconButton(
                    onClick = {
                        haptic.performLight()
                        onCreateNoteInFolder(item.folder)
                    },
                    modifier = Modifier.size(36.dp).testTag("folder_add_note_${item.folder.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "New note in folder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp).testTag("folder_menu_${item.folder.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Folder options"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Notes") },
                            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onOpenNotes(item.folder)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add Subfolder") },
                            leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onAddSubfolder(item.folder)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Folder") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit(item.folder)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Folder", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete(item.folder)
                            }
                        )
                    }
                }
            }
        }

        // Subfolders
        AnimatedVisibility(visible = expanded && item.subfolders.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.subfolders.forEach { sub ->
                    FolderTreeItem(
                        item = sub,
                        haptic = haptic,
                        onOpenNotes = onOpenNotes,
                        onCreateNoteInFolder = onCreateNoteInFolder,
                        onAddSubfolder = onAddSubfolder,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        indentationLevel = indentationLevel + 1
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderEditDialog(
    title: String,
    initialName: String,
    initialColorId: Int,
    availableParents: List<FolderEntity>,
    selectedParentId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, parentId: Long?, colorId: Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var colorId by remember { mutableIntStateOf(initialColorId) }
    var parentId by remember { mutableStateOf(selectedParentId) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("folder_name_input")
                )

                // Parent Folder Selection
                if (availableParents.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Parent Folder (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            val parentName = availableParents.firstOrNull { it.id == parentId }?.name ?: "None (Top Level)"
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { parentDropdownExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(parentName, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            DropdownMenu(
                                expanded = parentDropdownExpanded,
                                onDismissRequest = { parentDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (Top Level)") },
                                    onClick = {
                                        parentId = null
                                        parentDropdownExpanded = false
                                    }
                                )
                                availableParents.forEach { folder ->
                                    DropdownMenuItem(
                                        text = { Text(folder.name) },
                                        onClick = {
                                            parentId = folder.id
                                            parentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Folder Color
                Column {
                    Text(
                        text = "Folder Color",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExpressiveColorPicker(
                        selectedColorId = colorId,
                        onColorSelected = { colorId = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, parentId, colorId)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("confirm_folder_button")
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
