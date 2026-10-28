package com.example.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.repository.ViewMode
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExpressiveColorPicker
import com.example.ui.home.components.NoteCard
import com.example.ui.navigation.QuickJotRoute
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    haptic: HapticFeedbackHelper,
    onNavigate: (QuickJotRoute) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showColorPicker by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }

                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.setQuery(it) },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_text_input")
                    )

                    IconButton(onClick = { showOptions = !showOptions }) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Search Filters",
                            tint = if (showOptions) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scope Tabs (Active, Archive, Trash)
                TabRow(
                    selectedTabIndex = uiState.filters.scope.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = uiState.filters.scope == SearchScope.ACTIVE,
                        onClick = {
                            haptic.tick()
                            viewModel.setScope(SearchScope.ACTIVE)
                        },
                        text = { Text("Notes") }
                    )
                    Tab(
                        selected = uiState.filters.scope == SearchScope.ARCHIVE,
                        onClick = {
                            haptic.tick()
                            viewModel.setScope(SearchScope.ARCHIVE)
                        },
                        text = { Text(stringResource(R.string.nav_archive)) }
                    )
                    Tab(
                        selected = uiState.filters.scope == SearchScope.TRASH,
                        onClick = {
                            haptic.tick()
                            viewModel.setScope(SearchScope.TRASH)
                        },
                        text = { Text(stringResource(R.string.nav_trash)) }
                    )
                }

                // Filter options bar
                AnimatedVisibility(visible = showOptions) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Filter By",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = uiState.filters.isPinnedOnly,
                                    onClick = {
                                        haptic.tick()
                                        viewModel.togglePinnedOnly()
                                    },
                                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Pinned") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = uiState.filters.isChecklistOnly,
                                    onClick = {
                                        haptic.tick()
                                        viewModel.toggleChecklistOnly()
                                    },
                                    leadingIcon = { Icon(Icons.Default.CheckBox, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Checklists") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = uiState.filters.hasReminderOnly,
                                    onClick = {
                                        haptic.tick()
                                        viewModel.toggleReminderOnly()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Reminders") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = uiState.filters.isLockedOnly,
                                    onClick = {
                                        haptic.tick()
                                        viewModel.toggleLockedOnly()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Locked") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = uiState.filters.isCaseSensitive,
                                    onClick = {
                                        haptic.tick()
                                        viewModel.toggleCaseSensitive()
                                    },
                                    label = { Text("Case-sensitive") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = uiState.filters.isWholeWord,
                                    onClick = {
                                        haptic.tick()
                                        viewModel.toggleWholeWord()
                                    },
                                    label = { Text("Whole word") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = uiState.filters.colorId != null,
                                    onClick = { showColorPicker = true },
                                    label = { Text(if (uiState.filters.colorId != null) "Color: #${uiState.filters.colorId}" else "Color") }
                                )
                            }
                        }

                        // Tags row
                        if (uiState.allTags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(uiState.allTags) { tag ->
                                    FilterChip(
                                        selected = uiState.filters.tagId == tag.id,
                                        onClick = {
                                            haptic.tick()
                                            viewModel.setTagFilter(tag.id)
                                        },
                                        label = { Text("#${tag.name}") }
                                    )
                                }
                            }
                        }

                        // Clear filters button
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { viewModel.resetFilters() }) {
                                Text("Reset Filters")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.query.isEmpty() && uiState.recentSearches.isNotEmpty() && !showOptions) {
                // Recent Searches
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { viewModel.clearAllRecentSearches() }) {
                        Text("Clear All")
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.recentSearches) { recent ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.tick()
                                    viewModel.setQuery(recent.query)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(recent.query, style = MaterialTheme.typography.bodyLarge)
                            }
                            IconButton(onClick = { viewModel.clearRecentSearch(recent.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            } else if (uiState.results.isEmpty() && uiState.query.isNotEmpty()) {
                // Empty search results
                EmptyStateView(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.empty_search_title),
                    description = stringResource(R.string.empty_search_desc),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Search Results
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.results, key = { it.note.id }) { noteWithDetails ->
                        NoteCard(
                            noteWithDetails = noteWithDetails,
                            viewMode = ViewMode.LIST,
                            isSelected = false,
                            isInSelectionMode = false,
                            onClick = { onNavigate(QuickJotRoute.Editor(noteId = noteWithDetails.note.id)) },
                            onLongClick = {}
                        )
                    }
                }
            }
        }

        if (showColorPicker) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showColorPicker = false },
                title = { Text("Filter by Color") },
                text = {
                    ExpressiveColorPicker(
                        selectedColorId = uiState.filters.colorId ?: 0,
                        onColorSelected = { colorId ->
                            viewModel.setColorFilter(colorId)
                            showColorPicker = false
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.setColorFilter(null)
                        showColorPicker = false
                    }) {
                        Text("Clear Color")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showColorPicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
