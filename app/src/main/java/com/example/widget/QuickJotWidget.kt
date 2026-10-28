package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.QuickJotApplication
import com.example.data.local.models.NoteWithDetails
import kotlinx.coroutines.flow.first

class QuickJotWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? QuickJotApplication
        val notes = app?.container?.notesRepository?.activeNotesFlow?.first()?.take(3) ?: emptyList()

        provideContent {
            GlanceTheme {
                WidgetContent(context = context, notes = notes)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, notes: List<NoteWithDetails>) {
        val createIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ACTION_CREATE", true)
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(16.dp)
                .padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QuickJot",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onSurface
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Button(
                    text = "+ Jot",
                    onClick = actionStartActivity(createIntent)
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Notes list
            if (notes.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notes yet. Tap + Jot to start!",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            } else {
                notes.forEach { item ->
                    val openIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("EXTRA_OPEN_NOTE_ID", item.note.id)
                    }

                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .cornerRadius(8.dp)
                            .background(GlanceTheme.colors.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .clickable(actionStartActivity(openIntent))
                    ) {
                        Text(
                            text = item.note.title.ifBlank { "Untitled Note" },
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                        if (item.note.body.isNotBlank()) {
                            Text(
                                text = item.note.body,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = GlanceTheme.colors.onSurfaceVariant
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
