package com.example.di

import android.content.Context
import com.example.data.datastore.DefaultUserPreferencesRepository
import com.example.data.local.QuickJotDatabase
import com.example.data.repository.OfflineNotesRepository
import com.example.data.security.DatabaseKeyManager
import com.example.domain.repository.NotesRepository
import com.example.domain.repository.UserPreferencesRepository
import com.example.ui.util.HapticFeedbackHelper

/**
 * AppContainer provides dependencies for the application (Dependency Injection).
 * Ensures testability, decoupling, and zero runtime bytecode transform overhead.
 */
interface AppContainer {
    val context: Context
    val database: QuickJotDatabase
    val hapticFeedbackHelper: HapticFeedbackHelper
    val userPreferencesRepository: UserPreferencesRepository
    val notesRepository: NotesRepository
    val securityManager: com.example.data.security.SecurityManager
    val backupManager: com.example.data.backup.BackupManager
}

/**
 * Production implementation of AppContainer.
 * Repositories and database instances are lazily initialized singletons.
 */
class DefaultAppContainer(override val context: Context) : AppContainer {

    private val databaseKeyManager: DatabaseKeyManager by lazy {
        DatabaseKeyManager(context)
    }

    override val database: QuickJotDatabase by lazy {
        QuickJotDatabase.buildDatabase(
            context = context,
            keyManager = databaseKeyManager,
            inMemory = false
        )
    }

    override val hapticFeedbackHelper: HapticFeedbackHelper by lazy {
        HapticFeedbackHelper(context)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        DefaultUserPreferencesRepository(context)
    }

    override val notesRepository: NotesRepository by lazy {
        OfflineNotesRepository(database)
    }

    override val securityManager: com.example.data.security.SecurityManager by lazy {
        com.example.data.security.SecurityManager(context)
    }

    override val backupManager: com.example.data.backup.BackupManager by lazy {
        com.example.data.backup.BackupManager(notesRepository)
    }
}
