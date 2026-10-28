package com.example.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.repository.SortOption
import com.example.domain.repository.ViewMode
import com.example.ui.home.HomeFilter
import com.example.ui.util.HapticFeedbackHelper

@Composable
fun HomeTopBar(
    selectedCount: Int,
    isInSelectionMode: Boolean,
    currentFilter: HomeFilter,
    currentViewMode: ViewMode,
    currentSortOption: SortOption,
    isSortAscending: Boolean,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onToggleViewMode: () -> Unit,
    onSortSelected: (SortOption, Boolean) -> Unit,
    onFilterSelected: (HomeFilter) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onBulkDelete: () -> Unit,
    onBulkArchive: () -> Unit,
    onBulkPin: () -> Unit,
    onBulkColorClick: () -> Unit,
    haptic: HapticFeedbackHelper,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (isInSelectionMode) {
            // Multi-Select Action Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClearSelection) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.bulk_selected, selectedCount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onSelectAll) {
                            Icon(Icons.Default.SelectAll, contentDescription = stringResource(R.string.select_all))
                        }
                        IconButton(onClick = onBulkPin) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pin selected")
                        }
                        IconButton(onClick = onBulkColorClick) {
                            Icon(Icons.Default.ColorLens, contentDescription = "Color selected")
                        }
                        IconButton(onClick = onBulkArchive) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive selected")
                        }
                        IconButton(onClick = onBulkDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                    }
                }
            }
        } else {
            // Standard Home Search Bar / App Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.tick()
                            onMenuClick()
                        },
                        modifier = Modifier.testTag("nav_drawer_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_menu))
                    }

                    // Clickable Search Field
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                haptic.tick()
                                onSearchClick()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // View Mode Toggle
                    IconButton(
                        onClick = {
                            haptic.tick()
                            onToggleViewMode()
                        },
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    ) {
                        val icon = when (currentViewMode) {
                            ViewMode.LIST -> Icons.Default.GridView
                            ViewMode.GRID -> Icons.Default.ViewStream
                            ViewMode.COMPACT -> Icons.Default.ViewList
                        }
                        Icon(icon, contentDescription = stringResource(R.string.cd_view_mode))
                    }

                    // Sort dropdown
                    Box {
                        IconButton(
                            onClick = {
                                haptic.tick()
                                showSortMenu = true
                            },
                            modifier = Modifier.testTag("sort_menu_button")
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.cd_sort))
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Date Modified (Newest)") },
                                onClick = {
                                    onSortSelected(SortOption.MODIFIED, false)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Date Modified (Oldest)") },
                                onClick = {
                                    onSortSelected(SortOption.MODIFIED, true)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Date Created") },
                                onClick = {
                                    onSortSelected(SortOption.CREATED, false)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title (A-Z)") },
                                onClick = {
                                    onSortSelected(SortOption.TITLE, true)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Color") },
                                onClick = {
                                    onSortSelected(SortOption.COLOR, false)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Filter Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = currentFilter == HomeFilter.ALL,
                        onClick = {
                            haptic.tick()
                            onFilterSelected(HomeFilter.ALL)
                        },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == HomeFilter.PINNED,
                        onClick = {
                            haptic.tick()
                            onFilterSelected(HomeFilter.PINNED)
                        },
                        label = { Text(stringResource(R.string.filter_pinned)) }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == HomeFilter.CHECKLISTS,
                        onClick = {
                            haptic.tick()
                            onFilterSelected(HomeFilter.CHECKLISTS)
                        },
                        label = { Text(stringResource(R.string.filter_checklists)) }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == HomeFilter.REMINDERS,
                        onClick = {
                            haptic.tick()
                            onFilterSelected(HomeFilter.REMINDERS)
                        },
                        label = { Text(stringResource(R.string.filter_reminders)) }
                    )
                }
                item {
                    FilterChip(
                        selected = currentFilter == HomeFilter.LOCKED,
                        onClick = {
                            haptic.tick()
                            onFilterSelected(HomeFilter.LOCKED)
                        },
                        label = { Text(stringResource(R.string.filter_locked)) }
                    )
                }
            }
        }
    }
}
