package com.example.notes.feature.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.R
import com.example.notes.core.designsystem.components.ColorPicker
import com.example.notes.core.designsystem.components.ConfirmDialog
import com.example.notes.core.designsystem.theme.notes.PillShape
import com.example.notes.feature.folders.tags.TagsBottomSheet
import com.example.notes.feature.reminders.ReminderBottomSheet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    modifier: Modifier = Modifier,
    viewModel: NoteEditorViewModel,
    onBack: () -> Unit,
    navigateToNote: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                NoteEditorNavigationEvents.NavigateBack -> onBack()
                NoteEditorNavigationEvents.NavigateToNotes -> navigateToNote()
            }
        }
    }


    NoteEditorScreen(
        modifier = modifier,
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    modifier: Modifier,
    state: NoteEditorUiState,
    onEvent: (NoteEditorUiEvent) -> Unit,
) {

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.note_editor_screen_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(NoteEditorUiEvent.BackPressed) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(NoteEditorUiEvent.SaveNote) }) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .navigationBarsPadding()
                    ) {
                        AnimatedVisibility(visible = state.showColorPicker) {
                            Column {
                                ColorPicker(
                                    selectedColor = state.color,
                                    onColorSelected = { onEvent(NoteEditorUiEvent.ChooseNoteColor(it)) }
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    thickness = 0.5.dp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onEvent(NoteEditorUiEvent.AddCheckListItem) }) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckBox,
                                    contentDescription = "Add checklist",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEvent(NoteEditorUiEvent.ToggleColorPicker) }) {
                                Icon(
                                    imageVector = Icons.Outlined.ColorLens,
                                    contentDescription = "Color palette",
                                    tint = if (state.showColorPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEvent(NoteEditorUiEvent.TogglePin) }) {
                                Icon(
                                    imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (state.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEvent(NoteEditorUiEvent.ToggleReminderSheet) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Alarm,
                                    contentDescription = "Set reminder",
                                    tint = if (state.showReminderDialog) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEvent(NoteEditorUiEvent.ToggleArchive) }) {
                                Icon(
                                    imageVector = if (state.isArchived) Icons.Filled.Archive else Icons.Outlined.Archive,
                                    contentDescription = "Archive",
                                    tint = if (state.isArchived) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEvent(NoteEditorUiEvent.ToggleFavorite) }) {
                                Icon(
                                    imageVector = if (state.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (state.isFavorite) Color(0xFFFFA000) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEvent(NoteEditorUiEvent.ShowDeleteDialog) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = state.title,
                onValueChange = { onEvent(NoteEditorUiEvent.TitleChanged(it)) },
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (state.title.isEmpty()) {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.tags.forEach { item ->
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier.width(IntrinsicSize.Min)
                            ) {
                                Text(
                                    text = item.name,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    modifier = Modifier.widthIn(min = 30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove tag",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onEvent(NoteEditorUiEvent.RemoveTag(item)) }
                            )
                        }
                    }
                }

                Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .height(30.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .clickable(onClick = { onEvent(NoteEditorUiEvent.ToggleTagsSheet) })
                    ) {
                        Text(
                            text = "+ tag",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = state.content,
                onValueChange = { onEvent(NoteEditorUiEvent.ContentChanged(it)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (state.content.isEmpty() && state.checklist.isEmpty()) {
                        Text(
                            text = "Start writing...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (state.checklist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val completed = state.checklist.count { it.isDone }
                Text(
                    text = "$completed of ${state.checklist.size} complete",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                state.checklist.forEachIndexed { index, item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (item.isDone) MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.35f
                        )
                        else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = item.isDone,
                                onCheckedChange = { isChecked ->
                                    onEvent(
                                        NoteEditorUiEvent.UpdateCheckListItem(
                                            index,
                                            item.text,
                                            isChecked
                                        )
                                    )
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            BasicTextField(
                                value = item.text,
                                onValueChange = { newText ->
                                    onEvent(
                                        NoteEditorUiEvent.UpdateCheckListItem(
                                            index,
                                            newText,
                                            item.isDone
                                        )
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                            )

                            IconButton(
                                onClick = { onEvent(NoteEditorUiEvent.DeleteCheckListItem(index)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove item",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable { onEvent(NoteEditorUiEvent.AddCheckListItem) }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "+ Add an item",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

            }


            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .clickable { onEvent(NoteEditorUiEvent.ToggleFoldersSheet) }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = state.folderName ?: "Move to Folder",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove Folder",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onEvent(NoteEditorUiEvent.RemoveFolder) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }

    if (state.showReminderDialog) {
        ReminderBottomSheet(
            reminder = state.reminder,
            onDismiss = { onEvent(NoteEditorUiEvent.ToggleReminderSheet) },
            onSave = { timestamp, interval ->
                onEvent(NoteEditorUiEvent.SetReminder(timestamp, interval))
            }
        )
    }

    if (state.showTagPicker) {
        TagsBottomSheet(
            onDismiss = { onEvent(NoteEditorUiEvent.ToggleTagsSheet) },
            onSave = {
                onEvent(NoteEditorUiEvent.AddTag(it))
            }
        )
    }

    if (state.showFolderPicker) {
        FoldersBottomSheet(
            onDismiss = {
                onEvent(NoteEditorUiEvent.ToggleFoldersSheet)
            },
            onSave = {
                onEvent(NoteEditorUiEvent.FolderChange(it))
            }
        )
    }


    ConfirmDialog(
        show = state.showDeleteDialog,
        title = "Delete note?",
        message = "This note will be moved to trash",
        confirmText = "Delete",
        onConfirm = { onEvent(NoteEditorUiEvent.DeleteNote) },
        onDismiss = { onEvent(NoteEditorUiEvent.ShowDeleteDialog) }
    )
}
