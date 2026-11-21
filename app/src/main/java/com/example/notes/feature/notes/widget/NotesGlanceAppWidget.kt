package com.example.notes.feature.notes.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import com.example.notes.core.designsystem.theme.glance.NotesGlanceColorProvider
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import com.example.notes.MainActivity
import com.example.notes.R
import com.example.notes.core.data.repository.NoteRepository
import com.example.notes.core.model.Note
import com.example.notes.core.designsystem.theme.glance.LocalNotesGlancePalette
import com.example.notes.core.designsystem.theme.glance.NotesGlanceTextStyles
import com.example.notes.core.designsystem.theme.glance.getGlanceColorForNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext

class NotesGlanceAppWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val noteRepository = GlobalContext.get().get<NoteRepository>()
        val initialNotes = withContext(Dispatchers.IO) {
            noteRepository.getActiveNotes().first()
        }

        provideContent {
            val notes by noteRepository.getActiveNotes().collectAsState(initialNotes)
            val scope = rememberCoroutineScope()
            GlanceTheme(
                colors = NotesGlanceColorProvider.colors
            ) {
                CompositionLocalProvider(LocalNotesGlancePalette provides LocalNotesGlancePalette.current) {
                    NotesContent(
                        notes = notes,
                        onPinnedClicked = { noteId, isPinned ->
                            scope.launch { noteRepository.setPinned(noteId, isPinned) }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun NotesContent(
        modifier: GlanceModifier = GlanceModifier,
        notes: List<Note>,
        onPinnedClicked: (Long, Boolean) -> Unit
    ) {
        Column(
            modifier = modifier.fillMaxWidth().wrapContentHeight()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(24.dp),
        ) {
            Header(modifier = GlanceModifier.fillMaxWidth())

            key(LocalSize.current) {
                Body(notes = notes, onPinnedClicked = onPinnedClicked)
            }
        }
    }

    @Composable
    fun Header(modifier: GlanceModifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            val context = LocalContext.current
            Image(
                provider = ImageProvider(R.drawable.ic_star_filled),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                contentDescription = null,
                modifier = GlanceModifier.size(24.dp),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = context.getString(R.string.app_name),
                style = NotesGlanceTextStyles.bodyLarge.copy(
                    color = GlanceTheme.colors.onSurface
                ),
            )
        }
    }

    @Composable
    fun Body(
        notes: List<Note>,
        onPinnedClicked: (Long, Boolean) -> Unit
    ) {
        val size = LocalSize.current

        if (size.width < 182.dp && size.height < 261.dp) {
            NoteCard(
                note = notes.first(),
                onPinnedClicked = onPinnedClicked
            )
        } else {
            LazyColumn(modifier = GlanceModifier.wrapContentHeight().padding(8.dp)) {
                items(notes, itemId = { it.id }) { note ->
                    StripeNote(note)
                }
            }
        }
    }


    @Composable
    fun StripeNote(note: Note) {
        val context = LocalContext.current
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(14.dp)
                .background(GlanceTheme.colors.surface)
                .wrapContentHeight()
                .clickable(onClick = openNoteDetails(context = context, noteId = note.id)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .width(4.dp)
                    .height(44.dp)
                    .background(getGlanceColorForNote(note.color))
            ) {}

            Spacer(modifier = GlanceModifier.width(14.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title.ifEmpty { "Untitled" },
                        style = NotesGlanceTextStyles.bodyLarge.copy(
                            color = GlanceTheme.colors.onSurface
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }

                if (note.content.isNotBlank()) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = NotesGlanceTextStyles.bodySmall.copy(
                            color = GlanceTheme.colors.onSurface
                        ),
                        maxLines = 1,
                    )
                }
            }
        }
    }

    @Composable
    fun NoteCard(
        note: Note,
        onPinnedClicked: (Long, Boolean) -> Unit
    ) {
        val context = LocalContext.current

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight()
                .background(getGlanceColorForNote(note.color))
                .clickable(onClick = openNoteDetails(context = context, noteId = note.id))
        ) {
            // Header: Title & Pin Icon
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = NotesGlanceTextStyles.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 2,
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Image(
                    provider = ImageProvider(if (note.isPinned) R.drawable.baseline_push_pin_24 else R.drawable.outline_push_pin),
                    contentDescription = "Pinned",
                    colorFilter = ColorFilter.tint(if (note.isPinned) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier
                        .size(18.dp)
                        .clickable { onPinnedClicked(note.id, !note.isPinned) }
                )
            }

            if (note.content.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = note.content,
                    style = NotesGlanceTextStyles.bodyLarge.copy(
                        color = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 4,
                )
            }

            if (note.checklist.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                val completedCount = note.checklist.count { it.isDone }
                Text(
                    text = "☑ ${completedCount}/${note.checklist.size} complete",
                    style = NotesGlanceTextStyles.bodySmall.copy(
                        color = GlanceTheme.colors.onSurface
                    ),
                )
            }

            if (note.tags.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(12.dp))
                Row(
                    horizontalAlignment = Alignment.Horizontal.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier
                ) {
                    note.tags.take(2)
                        .forEach { tag ->
                            Box(
                                modifier = GlanceModifier.height(24.dp)
                                    .background(GlanceTheme.colors.surface)
                                    .cornerRadius(radius = 25.dp)
                            ) {
                                Text(
                                    text = "#${tag.name}",
                                    style = NotesGlanceTextStyles.bodySmall.copy(
                                        color = GlanceTheme.colors.onSurface,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = GlanceModifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(8.dp))
                        }
                }
            }
        }
    }

    private fun openNoteDetails(context: Context, noteId: Long): Action {
        return actionStartActivity(
            Intent(
                Intent.ACTION_VIEW,
                "mynotes://noteEditor/$noteId".toUri(),
                context,
                MainActivity::class.java,
            ),
        )
    }
}

