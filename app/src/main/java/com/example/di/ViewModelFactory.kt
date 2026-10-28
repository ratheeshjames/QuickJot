package com.example.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ui.archive.ArchiveViewModel
import com.example.ui.editor.EditorViewModel
import com.example.ui.folders.FoldersViewModel
import com.example.ui.home.HomeViewModel
import com.example.ui.onboarding.OnboardingViewModel
import com.example.ui.search.SearchViewModel
import com.example.ui.tags.TagsViewModel
import com.example.ui.trash.TrashViewModel

class ViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    notesRepository = appContainer.notesRepository,
                    userPreferencesRepository = appContainer.userPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                OnboardingViewModel(
                    userPreferencesRepository = appContainer.userPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(TrashViewModel::class.java) -> {
                TrashViewModel(
                    notesRepository = appContainer.notesRepository,
                    userPreferencesRepository = appContainer.userPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(ArchiveViewModel::class.java) -> {
                ArchiveViewModel(
                    notesRepository = appContainer.notesRepository
                ) as T
            }
            modelClass.isAssignableFrom(FoldersViewModel::class.java) -> {
                FoldersViewModel(
                    notesRepository = appContainer.notesRepository
                ) as T
            }
            modelClass.isAssignableFrom(TagsViewModel::class.java) -> {
                TagsViewModel(
                    notesRepository = appContainer.notesRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.example.ui.reminders.RemindersViewModel::class.java) -> {
                com.example.ui.reminders.RemindersViewModel(
                    notesRepository = appContainer.notesRepository,
                    context = appContainer.context
                ) as T
            }
            modelClass.isAssignableFrom(com.example.ui.settings.SettingsViewModel::class.java) -> {
                com.example.ui.settings.SettingsViewModel(
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    notesRepository = appContainer.notesRepository,
                    securityManager = appContainer.securityManager,
                    backupManager = appContainer.backupManager,
                    appContext = appContainer.context
                ) as T
            }
            modelClass.isAssignableFrom(com.example.ui.stats.StatsViewModel::class.java) -> {
                com.example.ui.stats.StatsViewModel(
                    notesRepository = appContainer.notesRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    fun createEditorViewModel(
        noteId: Long?,
        initialFolderId: Long?,
        initialType: String?,
        sharedText: String?
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditorViewModel(
                    notesRepository = appContainer.notesRepository,
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    initialNoteId = noteId,
                    initialFolderId = initialFolderId,
                    initialType = initialType,
                    initialSharedText = sharedText
                ) as T
            }
        }
    }

    fun createSearchViewModel(
        initialTag: String?,
        initialFolderId: Long?
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(
                    notesRepository = appContainer.notesRepository,
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    initialTag = initialTag,
                    initialFolderId = initialFolderId
                ) as T
            }
        }
    }

    fun createLockViewModel(reason: String): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return com.example.ui.security.LockViewModel(
                    securityManager = appContainer.securityManager,
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    reason = reason
                ) as T
            }
        }
    }
}
