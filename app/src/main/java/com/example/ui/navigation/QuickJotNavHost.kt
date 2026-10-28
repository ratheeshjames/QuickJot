package com.example.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.di.AppContainer
import com.example.di.ViewModelFactory
import com.example.ui.archive.ArchiveScreen
import com.example.ui.archive.ArchiveViewModel
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.folders.FoldersScreen
import com.example.ui.folders.FoldersViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.onboarding.OnboardingViewModel
import com.example.ui.search.SearchScreen
import com.example.ui.search.SearchViewModel
import com.example.ui.tags.TagsScreen
import com.example.ui.tags.TagsViewModel
import com.example.ui.trash.TrashScreen
import com.example.ui.trash.TrashViewModel

@Composable
fun QuickJotNavHost(
    navController: NavHostController,
    container: AppContainer,
    startDestination: QuickJotRoute = QuickJotRoute.Home,
    modifier: Modifier = Modifier
) {
    val factory = ViewModelFactory(container)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut() }
    ) {
        composable<QuickJotRoute.Onboarding> {
            val onboardingViewModel: OnboardingViewModel = viewModel(factory = factory)
            OnboardingScreen(
                viewModel = onboardingViewModel,
                hapticFeedbackHelper = container.hapticFeedbackHelper,
                onFinish = {
                    navController.navigate(QuickJotRoute.Home) {
                        popUpTo(QuickJotRoute.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<QuickJotRoute.Home> {
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                viewModel = homeViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable<QuickJotRoute.Editor> { backStackEntry ->
            val route = backStackEntry.toRoute<QuickJotRoute.Editor>()
            val editorViewModel: EditorViewModel = viewModel(
                factory = factory.createEditorViewModel(
                    noteId = route.noteId,
                    initialFolderId = route.initialFolderId,
                    initialType = route.initialType,
                    sharedText = route.sharedText
                )
            )
            EditorScreen(
                viewModel = editorViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<QuickJotRoute.Search> { backStackEntry ->
            val route = backStackEntry.toRoute<QuickJotRoute.Search>()
            val searchViewModel: SearchViewModel = viewModel(
                factory = factory.createSearchViewModel(
                    initialTag = route.initialTag,
                    initialFolderId = route.initialFolderId
                )
            )
            SearchScreen(
                viewModel = searchViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigate = { navController.navigate(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QuickJotRoute.Trash> {
            val trashViewModel: TrashViewModel = viewModel(factory = factory)
            TrashScreen(
                viewModel = trashViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QuickJotRoute.Archive> {
            val archiveViewModel: ArchiveViewModel = viewModel(factory = factory)
            ArchiveScreen(
                viewModel = archiveViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigate = { navController.navigate(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QuickJotRoute.Folders> {
            val foldersViewModel: FoldersViewModel = viewModel(factory = factory)
            FoldersScreen(
                viewModel = foldersViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigate = { navController.navigate(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QuickJotRoute.Tags> {
            val tagsViewModel: TagsViewModel = viewModel(factory = factory)
            TagsScreen(
                viewModel = tagsViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigate = { navController.navigate(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QuickJotRoute.Reminders> {
            val remindersViewModel: com.example.ui.reminders.RemindersViewModel = viewModel(factory = factory)
            com.example.ui.reminders.RemindersScreen(
                viewModel = remindersViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigate = { navController.navigate(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QuickJotRoute.Settings> {
            val settingsViewModel: com.example.ui.settings.SettingsViewModel = viewModel(factory = factory)
            com.example.ui.settings.SettingsScreen(
                viewModel = settingsViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigate = { navController.navigate(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QuickJotRoute.Stats> {
            val statsViewModel: com.example.ui.stats.StatsViewModel = viewModel(factory = factory)
            com.example.ui.stats.StatsScreen(
                viewModel = statsViewModel,
                haptic = container.hapticFeedbackHelper,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QuickJotRoute.Help> {
            com.example.ui.help.HelpScreen(
                haptic = container.hapticFeedbackHelper,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<QuickJotRoute.LockScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<QuickJotRoute.LockScreen>()
            val lockViewModel: com.example.ui.security.LockViewModel = viewModel(
                factory = factory.createLockViewModel(reason = route.reason)
            )
            com.example.ui.security.LockScreen(
                viewModel = lockViewModel,
                haptic = container.hapticFeedbackHelper,
                onUnlocked = {
                    if (route.reason == "app_lock") {
                        navController.navigate(QuickJotRoute.Home) {
                            popUpTo(QuickJotRoute.LockScreen) { inclusive = true }
                        }
                    } else if (route.reason.startsWith("note_lock:")) {
                        val noteId = route.reason.removePrefix("note_lock:").toLongOrNull()
                        if (noteId != null) {
                            navController.navigate(QuickJotRoute.Editor(noteId = noteId)) {
                                popUpTo(QuickJotRoute.LockScreen) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
