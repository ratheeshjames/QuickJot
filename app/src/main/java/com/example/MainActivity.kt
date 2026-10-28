package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.domain.repository.ThemeMode
import com.example.domain.repository.UserPreferences
import com.example.ui.navigation.QuickJotNavHost
import com.example.ui.navigation.QuickJotRoute
import com.example.ui.theme.QuickJotTheme
import com.example.util.DynamicShortcutsHelper

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as QuickJotApplication
        val container = app.container

        // Initialize dynamic launcher shortcuts
        DynamicShortcutsHelper.initDynamicShortcuts(this)

        // Extract intent extras
        val sharedText = if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else null

        val isCreateRequested = intent?.getBooleanExtra("EXTRA_ACTION_CREATE", false) == true
        val initialType = intent?.getStringExtra("EXTRA_INITIAL_TYPE")
        val isSearchRequested = intent?.getBooleanExtra("EXTRA_ACTION_SEARCH", false) == true
        val openNoteId = intent?.getLongExtra("EXTRA_OPEN_NOTE_ID", -1L)?.takeIf { it > 0 }

        setContent {
            val userPrefs by container.userPreferencesRepository.preferencesFlow.collectAsState(
                initial = UserPreferences()
            )

            // Dynamic FLAG_SECURE enforcement
            LaunchedEffect(userPrefs.screenshotBlocked) {
                if (userPrefs.screenshotBlocked) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            // Hide in recents (task switcher privacy)
            LaunchedEffect(userPrefs.hideInRecents) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    setRecentsScreenshotEnabled(!userPrefs.hideInRecents)
                }
            }

            val isDark = when (userPrefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            QuickJotTheme(
                darkTheme = isDark,
                dynamicColor = userPrefs.dynamicColor,
                accentChoice = userPrefs.accentChoice
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    val isAppLocked = userPrefs.onboardingCompleted &&
                            (userPrefs.pinHash != null || userPrefs.biometricEnabled) &&
                            !container.securityManager.isAppUnlocked(
                                autoLockTimeoutSeconds = userPrefs.autoLockTimeoutSeconds,
                                pinHash = userPrefs.pinHash,
                                biometricEnabled = userPrefs.biometricEnabled
                            )

                    val startDestination: QuickJotRoute = when {
                        !userPrefs.onboardingCompleted -> QuickJotRoute.Onboarding
                        isAppLocked -> QuickJotRoute.LockScreen(reason = "app_lock")
                        openNoteId != null -> QuickJotRoute.Editor(noteId = openNoteId)
                        sharedText != null -> QuickJotRoute.Editor(sharedText = sharedText)
                        isSearchRequested -> QuickJotRoute.Search()
                        isCreateRequested -> QuickJotRoute.Editor(initialType = initialType)
                        else -> QuickJotRoute.Home
                    }

                    QuickJotNavHost(
                        navController = navController,
                        container = container,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (application as QuickJotApplication).container.securityManager.recordUserActivity()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        (application as QuickJotApplication).container.securityManager.recordUserActivity()
    }
}
