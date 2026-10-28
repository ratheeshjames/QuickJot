package com.example.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for QuickJot.
 */
sealed interface QuickJotRoute {
    @Serializable
    data object Home : QuickJotRoute

    @Serializable
    data class Editor(
        val noteId: Long? = null,
        val initialFolderId: Long? = null,
        val initialType: String? = null, // "text", "checklist", "voice", "clipboard"
        val sharedText: String? = null
    ) : QuickJotRoute

    @Serializable
    data class Search(
        val initialTag: String? = null,
        val initialFolderId: Long? = null
    ) : QuickJotRoute

    @Serializable
    data object Trash : QuickJotRoute

    @Serializable
    data object Archive : QuickJotRoute

    @Serializable
    data object Folders : QuickJotRoute

    @Serializable
    data object Tags : QuickJotRoute

    @Serializable
    data object Reminders : QuickJotRoute

    @Serializable
    data object Settings : QuickJotRoute

    @Serializable
    data object Stats : QuickJotRoute

    @Serializable
    data object Help : QuickJotRoute

    @Serializable
    data object Onboarding : QuickJotRoute

    @Serializable
    data class LockScreen(
        val reason: String = "app_lock"
    ) : QuickJotRoute
}
