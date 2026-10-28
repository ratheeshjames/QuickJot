package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R

object DynamicShortcutsHelper {

    fun initDynamicShortcuts(context: Context) {
        val newNoteIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ACTION_CREATE", true)
        }

        val newChecklistIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ACTION_CREATE", true)
            putExtra("EXTRA_INITIAL_TYPE", "checklist")
        }

        val searchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ACTION_SEARCH", true)
        }

        val shortcutNewNote = ShortcutInfoCompat.Builder(context, "shortcut_new_note")
            .setShortLabel("New Note")
            .setLongLabel("Create a quick note")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(newNoteIntent)
            .build()

        val shortcutChecklist = ShortcutInfoCompat.Builder(context, "shortcut_new_checklist")
            .setShortLabel("Checklist")
            .setLongLabel("Create a checklist")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(newChecklistIntent)
            .build()

        val shortcutSearch = ShortcutInfoCompat.Builder(context, "shortcut_search")
            .setShortLabel("Search")
            .setLongLabel("Search your notes")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(searchIntent)
            .build()

        ShortcutManagerCompat.setDynamicShortcuts(
            context,
            listOf(shortcutNewNote, shortcutChecklist, shortcutSearch)
        )
    }
}
