package com.example.ui.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.repository.ThemeMode
import com.example.domain.repository.ViewMode
import com.example.ui.theme.AccentColorChoice
import com.example.ui.util.HapticFeedbackHelper
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    hapticFeedbackHelper: HapticFeedbackHelper,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userPrefs by viewModel.userPreferences.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) } // 1..7
    val totalSteps = 7

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (step in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (step == currentStep) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step == currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                if (currentStep < totalSteps) {
                    TextButton(
                        onClick = {
                            hapticFeedbackHelper.click()
                            viewModel.completeOnboarding(onFinish)
                        }
                    ) {
                        Text(stringResource(R.string.onboarding_skip))
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            label = "OnboardingStepTransition"
        ) { step ->
            when (step) {
                1 -> WelcomeStep(
                    onGetStarted = {
                        hapticFeedbackHelper.click()
                        currentStep = 2
                    },
                    onSkip = {
                        hapticFeedbackHelper.click()
                        viewModel.completeOnboarding(onFinish)
                    }
                )
                2 -> FeatureTourStep(
                    hapticFeedbackHelper = hapticFeedbackHelper,
                    onNext = {
                        hapticFeedbackHelper.click()
                        currentStep = 3
                    }
                )
                3 -> PermissionsStep(
                    onNext = {
                        hapticFeedbackHelper.click()
                        currentStep = 4
                    }
                )
                4 -> PersonalizeStep(
                    currentTheme = userPrefs.themeMode,
                    currentViewMode = userPrefs.viewMode,
                    currentAccent = userPrefs.accentChoice,
                    onThemeChange = { viewModel.setThemeMode(it) },
                    onViewModeChange = { viewModel.setViewMode(it) },
                    onAccentChange = { viewModel.setAccentChoice(it) },
                    haptic = hapticFeedbackHelper,
                    onNext = {
                        hapticFeedbackHelper.click()
                        currentStep = 5
                    }
                )
                5 -> SecurityStep(
                    biometricEnabled = userPrefs.biometricEnabled,
                    onToggleBiometric = { viewModel.setBiometricEnabled(it) },
                    onSavePin = { viewModel.setPin(it) },
                    haptic = hapticFeedbackHelper,
                    onNext = {
                        hapticFeedbackHelper.click()
                        currentStep = 6
                    }
                )
                6 -> BackupStep(
                    currentSchedule = userPrefs.backupSchedule,
                    onScheduleChange = { viewModel.setBackupSchedule(it) },
                    haptic = hapticFeedbackHelper,
                    onNext = {
                        hapticFeedbackHelper.click()
                        currentStep = 7
                    }
                )
                7 -> ReadyStep(
                    onFinish = {
                        hapticFeedbackHelper.heavyClick()
                        viewModel.completeOnboarding(onFinish)
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1: WELCOME
// -------------------------------------------------------------
@Composable
private fun WelcomeStep(
    onGetStarted: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(112.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = stringResource(R.string.onboarding_get_started),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2: FEATURE TOUR (3 Swipeable Cards)
// -------------------------------------------------------------
@Composable
private fun FeatureTourStep(
    hapticFeedbackHelper: HapticFeedbackHelper,
    onNext: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val tourCards = listOf(
        Triple(
            Icons.Default.EditNote,
            stringResource(R.string.tour_1_title),
            stringResource(R.string.tour_1_desc)
        ),
        Triple(
            Icons.Default.Folder,
            stringResource(R.string.tour_2_title),
            stringResource(R.string.tour_2_desc)
        ),
        Triple(
            Icons.Default.DeleteSweep,
            stringResource(R.string.tour_3_title),
            stringResource(R.string.tour_3_desc)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Discover QuickJot",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val item = tourCards[page]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(300.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.first,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = item.second,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = item.third,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Indicator dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until 3) {
                    Box(
                        modifier = Modifier
                            .size(if (i == pagerState.currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }

        Button(
            onClick = {
                if (pagerState.currentPage < 2) {
                    hapticFeedbackHelper.tick()
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                if (pagerState.currentPage < 2) stringResource(R.string.onboarding_next)
                else stringResource(R.string.onboarding_next),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// -------------------------------------------------------------
// STEP 3: PERMISSIONS
// -------------------------------------------------------------
@Composable
private fun PermissionsStep(
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var notifGranted by remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notifGranted = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Permissions & Power",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "QuickJot is 100% offline. Permissions are requested only for on-device features you use.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Notification permission item
            PermissionCard(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.permission_notif_title),
                description = stringResource(R.string.permission_notif_desc),
                actionLabel = if (notifGranted) "Granted" else stringResource(R.string.permission_allow),
                isGranted = notifGranted,
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notifGranted = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Exact Alarm permission item
            PermissionCard(
                icon = Icons.Default.Alarm,
                title = stringResource(R.string.permission_alarm_title),
                description = stringResource(R.string.permission_alarm_desc),
                actionLabel = "Enable in System",
                isGranted = false,
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone note
            PermissionCard(
                icon = Icons.Default.Mic,
                title = stringResource(R.string.permission_mic_title),
                description = stringResource(R.string.permission_mic_desc),
                actionLabel = "On First Use",
                isGranted = true,
                onAction = {}
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(R.string.onboarding_next), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    isGranted: Boolean,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            } else {
                TextButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 4: PERSONALIZE
// -------------------------------------------------------------
@Composable
private fun PersonalizeStep(
    currentTheme: ThemeMode,
    currentViewMode: ViewMode,
    currentAccent: AccentColorChoice,
    onThemeChange: (ThemeMode) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    onAccentChange: (AccentColorChoice) -> Unit,
    haptic: HapticFeedbackHelper,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.personalize_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Theme choices
            Text(
                text = stringResource(R.string.personalize_theme_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = currentTheme == mode,
                        onClick = {
                            haptic.tick()
                            onThemeChange(mode)
                        },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // View mode choices
            Text(
                text = stringResource(R.string.personalize_layout_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewMode.entries.forEach { mode ->
                    val icon = when (mode) {
                        ViewMode.LIST -> Icons.Default.ViewList
                        ViewMode.GRID -> Icons.Default.GridView
                        ViewMode.COMPACT -> Icons.Default.ViewStream
                    }
                    FilterChip(
                        selected = currentViewMode == mode,
                        onClick = {
                            haptic.tick()
                            onViewModeChange(mode)
                        },
                        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Accent color choices
            Text(
                text = stringResource(R.string.personalize_accent_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AccentColorChoice.entries.forEach { accent ->
                    val isSelected = currentAccent == accent
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accent.primaryColor)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .clickable {
                                haptic.tick()
                                onAccentChange(accent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(R.string.onboarding_next), style = MaterialTheme.typography.titleMedium)
        }
    }
}

// -------------------------------------------------------------
// STEP 5: OPTIONAL SECURITY
// -------------------------------------------------------------
@Composable
private fun SecurityStep(
    biometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onSavePin: (String) -> Unit,
    haptic: HapticFeedbackHelper,
    onNext: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    var pinSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.security_setup_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.security_setup_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Biometric switch card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = stringResource(R.string.security_setup_enable_bio), style = MaterialTheme.typography.titleMedium)
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = {
                            haptic.tick()
                            onToggleBiometric(it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PIN setup
            OutlinedTextField(
                value = pinText,
                onValueChange = {
                    if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                        pinText = it
                    }
                },
                label = { Text(stringResource(R.string.security_setup_set_pin)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            if (pinText.length == 4 && !pinSaved) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        haptic.click()
                        onSavePin(pinText)
                        pinSaved = true
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save PIN")
                }
            } else if (pinSaved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("PIN Saved!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(R.string.onboarding_next), style = MaterialTheme.typography.titleMedium)
        }
    }
}

// -------------------------------------------------------------
// STEP 6: BACKUP
// -------------------------------------------------------------
@Composable
private fun BackupStep(
    currentSchedule: String,
    onScheduleChange: (String) -> Unit,
    haptic: HapticFeedbackHelper,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Local Device Backup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "QuickJot can automatically create encrypted or plain JSON/ZIP backups to a folder on your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Automatic Backup Frequency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val schedules = listOf("OFF", "DAILY", "WEEKLY")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                schedules.forEach { sched ->
                    FilterChip(
                        selected = currentSchedule == sched,
                        onClick = {
                            haptic.tick()
                            onScheduleChange(sched)
                        },
                        label = { Text(sched) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(R.string.onboarding_next), style = MaterialTheme.typography.titleMedium)
        }
    }
}

// -------------------------------------------------------------
// STEP 7: READY & TIP
// -------------------------------------------------------------
@Composable
private fun ReadyStep(
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "You're All Set!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your workspace is ready. Tap the + FAB on the home screen to jot down your thoughts.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Tip: Highlight text in any app and choose 'Share to QuickJot' to instantly capture it as a note!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(R.string.onboarding_finish), style = MaterialTheme.typography.titleMedium)
        }
    }
}
