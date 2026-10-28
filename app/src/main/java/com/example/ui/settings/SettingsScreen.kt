package com.example.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.RestoreResult
import com.example.domain.repository.ThemeMode
import com.example.domain.repository.ViewMode
import com.example.ui.navigation.QuickJotRoute
import com.example.ui.theme.AccentColorChoice
import com.example.ui.util.HapticFeedbackHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    haptic: HapticFeedbackHelper,
    onNavigate: (QuickJotRoute) -> Unit,
    onNavigateBack: () -> Unit
) {
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showPinDialog by remember { mutableStateOf(false) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showExportPasswordDialog by remember { mutableStateOf<Uri?>(null) }
    var showRestorePasswordDialog by remember { mutableStateOf<Uri?>(null) }

    // SAF launchers
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.setBackupFolderUri(uri.toString())
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Backup folder configured")
            }
        }
    }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            showExportPasswordDialog = uri
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            showRestorePasswordDialog = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.click()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("settings_back_button")
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance & Theming
            item {
                SettingsSectionHeader(title = "Appearance & Style", icon = Icons.Default.Palette)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.values().forEach { mode ->
                                val isSelected = prefs.themeMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.click()
                                        viewModel.setThemeMode(mode)
                                    },
                                    label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Dynamic Color", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = "Adapt app colors from system wallpaper",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = prefs.dynamicColor,
                                    onCheckedChange = {
                                        haptic.click()
                                        viewModel.setDynamicColor(it)
                                    }
                                )
                            }
                        }

                        // Accent Color choices
                        AnimatedVisibility(visible = !prefs.dynamicColor || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Accent Color",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    AccentColorChoice.entries.forEach { accent ->
                                        val color = accent.primaryColor
                                        val isSelected = prefs.accentChoice == accent
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 3.dp else 0.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    haptic.click()
                                                    viewModel.setAccentChoice(accent)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Display & Default Note Preferences
            item {
                SettingsSectionHeader(title = "Notes & Display", icon = Icons.Default.ColorLens)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Default View Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ViewMode.values().forEach { mode ->
                                val isSelected = prefs.viewMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.click()
                                        viewModel.setViewMode(mode)
                                    },
                                    label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Character Limit
                        Text(
                            text = "Note Character Limit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val limits = listOf(500, 1000, 2000, 5000, 0)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            limits.forEach { limit ->
                                val isSelected = prefs.characterLimit == limit
                                val label = if (limit == 0) "None" else "$limit"
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.click()
                                        viewModel.setCharacterLimit(limit)
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Haptic feedback toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Haptic Feedback", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Vibrations on clicks, deletions, and toggles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = prefs.hapticsEnabled,
                                onCheckedChange = {
                                    haptic.click()
                                    viewModel.setHapticsEnabled(it)
                                }
                            )
                        }
                    }
                }
            }

            // Security & Privacy
            item {
                SettingsSectionHeader(title = "Security & Privacy", icon = Icons.Default.Security)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "App PIN Lock",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (prefs.pinHash != null) "PIN lock is active" else "Protect app launch with 4-digit PIN",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (prefs.pinHash != null) {
                                OutlinedButton(
                                    onClick = {
                                        haptic.click()
                                        viewModel.removePin()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("PIN removed")
                                        }
                                    }
                                ) {
                                    Text("Remove")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        haptic.click()
                                        showPinDialog = true
                                    }
                                ) {
                                    Text("Set PIN")
                                }
                            }
                        }

                        // Biometrics
                        if (prefs.pinHash != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Biometric Unlock", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = "Unlock with fingerprint or face prompt",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = prefs.biometricEnabled,
                                    onCheckedChange = {
                                        haptic.click()
                                        viewModel.setBiometricEnabled(it)
                                    }
                                )
                            }

                            // Auto Lock Timeout
                            Text(
                                text = "Auto-Lock Timeout",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val timeouts = listOf(0 to "Immediate", 30 to "30s", 60 to "1m", 300 to "5m", 900 to "15m")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                timeouts.forEach { (sec, label) ->
                                    val isSelected = prefs.autoLockTimeoutSeconds == sec
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.click()
                                            viewModel.setAutoLockTimeoutSeconds(sec)
                                        },
                                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Screenshot blocking
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Block Screenshots", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Prevent screen captures and recording (FLAG_SECURE)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = prefs.screenshotBlocked,
                                onCheckedChange = {
                                    haptic.click()
                                    viewModel.setScreenshotBlocked(it)
                                }
                            )
                        }

                        // Hide in recents
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Hide In App Switcher", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Blank preview in recent tasks screen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = prefs.hideInRecents,
                                onCheckedChange = {
                                    haptic.click()
                                    viewModel.setHideInRecents(it)
                                }
                            )
                        }
                    }
                }
            }

            // Trash & Retention
            item {
                SettingsSectionHeader(title = "Trash & Retention", icon = Icons.Default.Delete)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Auto-Purge Trash After",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val retentions = listOf(7, 14, 30)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            retentions.forEach { days ->
                                val isSelected = prefs.trashRetentionDays == days
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.click()
                                        viewModel.setTrashRetentionDays(days)
                                    },
                                    label = { Text("$days Days") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.click()
                                showEmptyTrashDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Empty Trash Now")
                        }
                    }
                }
            }

            // Backup & Restore
            item {
                SettingsSectionHeader(title = "Backup & Restore", icon = Icons.Default.Storage)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Scheduled Automatic Backups",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val schedules = listOf("OFF" to "Off", "DAILY" to "Daily", "WEEKLY" to "Weekly")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            schedules.forEach { (key, label) ->
                                val isSelected = prefs.backupSchedule == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.click()
                                        if (key != "OFF" && prefs.backupFolderUri == null) {
                                            folderPickerLauncher.launch(null)
                                        }
                                        viewModel.setBackupSchedule(key)
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.click()
                                folderPickerLauncher.launch(null)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (prefs.backupFolderUri != null) "Change Backup Folder" else "Choose Backup Folder"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    haptic.click()
                                    val timestamp = System.currentTimeMillis()
                                    createBackupLauncher.launch("quickjot_backup_$timestamp.qjbak")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Export")
                            }

                            FilledTonalButton(
                                onClick = {
                                    haptic.click()
                                    restoreBackupLauncher.launch(arrayOf("*/*"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Restore")
                            }
                        }
                    }
                }
            }

            // Statistics & Help Quick Links
            item {
                SettingsSectionHeader(title = "Information & Help", icon = Icons.Default.Info)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.click()
                                    onNavigate(QuickJotRoute.Stats)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.QueryStats, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("QuickJot Statistics", style = MaterialTheme.typography.titleMedium)
                                Text("View word counts, note counts, and checklist metrics", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.click()
                                    onNavigate(QuickJotRoute.Help)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Help & Gestures Guide", style = MaterialTheme.typography.titleMedium)
                                Text("Tips on gestures, formatting, reminders, and offline security", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // About footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QuickJot v1.0",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "100% Offline • Encrypted SQLCipher • Zero Tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // PIN Setup Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a 4-digit PIN to secure your notes.")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPinInput = it },
                        label = { Text("Confirm PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMsg != null) {
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length != 4) {
                            errorMsg = "PIN must be 4 digits"
                            return@Button
                        }
                        if (pinInput != confirmPinInput) {
                            errorMsg = "PINs do not match"
                            return@Button
                        }
                        haptic.click()
                        viewModel.savePin(pinInput)
                        showPinDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("PIN enabled successfully")
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Empty Trash confirmation dialog
    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty Trash?") },
            text = { Text("All notes in Trash will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.heavyClick()
                        viewModel.emptyTrash {
                            showEmptyTrashDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Trash emptied")
                            }
                        }
                    }
                ) {
                    Text("Empty")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Password Dialog (Optional encryption)
    showExportPasswordDialog?.let { targetUri ->
        var passwordInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showExportPasswordDialog = null },
            title = { Text("Export Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Leave password blank for unencrypted JSON backup, or enter a password to encrypt with AES-256-GCM.")
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Optional Encryption Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pwd = passwordInput.ifBlank { null }
                        showExportPasswordDialog = null
                        viewModel.exportBackup(context, targetUri, pwd) { success ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (success) "Backup exported successfully" else "Failed to export backup"
                                )
                            }
                        }
                    }
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportPasswordDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Password Dialog
    showRestorePasswordDialog?.let { targetUri ->
        var passwordInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRestorePasswordDialog = null },
            title = { Text("Restore Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("If the backup is encrypted, enter the password. Otherwise leave blank.")
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password (if encrypted)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pwd = passwordInput.ifBlank { null }
                        showRestorePasswordDialog = null
                        viewModel.restoreBackup(context, targetUri, pwd) { result ->
                            coroutineScope.launch {
                                when (result) {
                                    is RestoreResult.Success -> {
                                        snackbarHostState.showSnackbar(
                                            "Restored ${result.notesCount} notes and ${result.foldersCount} folders"
                                        )
                                    }
                                    is RestoreResult.Error -> {
                                        snackbarHostState.showSnackbar("Restore error: ${result.message}")
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestorePasswordDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
