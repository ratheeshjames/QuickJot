package com.example.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.navigation.QuickJotRoute

@Composable
fun HomeNavigationDrawer(
    currentRoute: QuickJotRoute,
    onNavigate: (QuickJotRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(320.dp),
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // App Title Header
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Main destinations
            DrawerItem(
                label = stringResource(R.string.nav_notes),
                icon = Icons.Default.EditNote,
                selected = currentRoute == QuickJotRoute.Home,
                onClick = { onNavigate(QuickJotRoute.Home) }
            )

            DrawerItem(
                label = stringResource(R.string.nav_reminders),
                icon = Icons.Default.Alarm,
                selected = currentRoute == QuickJotRoute.Reminders,
                onClick = { onNavigate(QuickJotRoute.Reminders) }
            )

            DrawerItem(
                label = stringResource(R.string.nav_folders),
                icon = Icons.Default.Folder,
                selected = currentRoute == QuickJotRoute.Folders,
                onClick = { onNavigate(QuickJotRoute.Folders) }
            )

            DrawerItem(
                label = stringResource(R.string.nav_tags),
                icon = Icons.Default.Label,
                selected = currentRoute == QuickJotRoute.Tags,
                onClick = { onNavigate(QuickJotRoute.Tags) }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            DrawerItem(
                label = stringResource(R.string.nav_archive),
                icon = Icons.Default.Archive,
                selected = currentRoute == QuickJotRoute.Archive,
                onClick = { onNavigate(QuickJotRoute.Archive) }
            )

            DrawerItem(
                label = stringResource(R.string.nav_trash),
                icon = Icons.Default.Delete,
                selected = currentRoute == QuickJotRoute.Trash,
                onClick = { onNavigate(QuickJotRoute.Trash) }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            DrawerItem(
                label = stringResource(R.string.nav_settings),
                icon = Icons.Default.Settings,
                selected = currentRoute == QuickJotRoute.Settings,
                onClick = { onNavigate(QuickJotRoute.Settings) }
            )

            DrawerItem(
                label = stringResource(R.string.nav_stats),
                icon = Icons.Default.BarChart,
                selected = currentRoute == QuickJotRoute.Stats,
                onClick = { onNavigate(QuickJotRoute.Stats) }
            )

            DrawerItem(
                label = stringResource(R.string.nav_help),
                icon = Icons.Default.HelpOutline,
                selected = currentRoute == QuickJotRoute.Help,
                onClick = { onNavigate(QuickJotRoute.Help) }
            )
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(text = label, style = MaterialTheme.typography.titleMedium) },
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
