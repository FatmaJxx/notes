package com.example.notes.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.notes.R
import com.example.notes.core.data.repository.FolderRepository
import com.example.notes.core.designsystem.theme.notes.CardShape
import com.example.notes.core.designsystem.theme.notes.DialogShape
import com.example.notes.core.designsystem.theme.notes.PillShape
import com.example.notes.core.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: FoldersSheetViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    onSave: (Folder) -> Unit
) {
    val state by viewModel.state
        .collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .padding(16.dp)
        ) {
            FoldersList(
                state = state,
                onEvent = viewModel::onEvent,
                onSave = onSave
            )
        }
    }
}

@Composable
fun FoldersList(
    state: FoldersSheetUiState,
    onEvent: (FoldersSheetUiEvent) -> Unit,
    onSave: (Folder) -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEvent(FoldersSheetUiEvent.ShowCreateDialog)
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.feature_folder_sheet_create_new_folder),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.folders, key = { it.id }) { folder ->
            Card(
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onSave(folder) })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            onEvent(FoldersSheetUiEvent.DeleteFolder(folder))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Folder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(FoldersSheetUiEvent.DismissCreateDialog) },
            shape = DialogShape,
            modifier = Modifier.padding(16.dp),
            title = { Text(stringResource(R.string.feature_folder_sheet_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = state.newFolderName,
                    onValueChange = { onEvent(FoldersSheetUiEvent.NewFolderNameChanged(it)) },
                    label = { Text(stringResource(R.string.feature_folder_sheet_dialog_text_field_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEvent(FoldersSheetUiEvent.CreateFolder)
                    },
                    shape = PillShape
                ) {
                    Text(stringResource(R.string.feature_folder_sheet_dialog_btn_action_create))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(FoldersSheetUiEvent.DismissCreateDialog) }) {
                    Text(stringResource(R.string.feature_folder_sheet_dialog_btn_action_cancel))
                }
            }
        )
    }
}

class FoldersSheetViewModel(
    private val foldersRepository: FolderRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FoldersSheetUiState())

    val state: StateFlow<FoldersSheetUiState> = combine(
        foldersRepository.getAllFolders(),
        _state
    ) { folders, currState ->
        currState.copy(
            folders = folders
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FoldersSheetUiState()
        )

    fun onEvent(event: FoldersSheetUiEvent) {
        when (event) {
            FoldersSheetUiEvent.CreateFolder -> createNewFolder(_state.value.newFolderName)
            is FoldersSheetUiEvent.DeleteFolder -> {
                deleteFolder(event.folder)
            }

            FoldersSheetUiEvent.DismissCreateDialog -> {
                _state.update { it.copy(showCreateDialog = false) }
            }

            is FoldersSheetUiEvent.NewFolderNameChanged -> {
                _state.update { it.copy(newFolderName = event.name) }
            }

            FoldersSheetUiEvent.ShowCreateDialog -> {
                _state.update { it.copy(showCreateDialog = true) }
            }
        }
    }

    private fun createNewFolder(name: String) {
        viewModelScope.launch {
            foldersRepository.createFolder(Folder(name = name))
            _state.update { it.copy(showCreateDialog = false, newFolderName = "") }
        }
    }

    private fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            foldersRepository.deleteFolder(folder)
        }
    }
}

data class FoldersSheetUiState(
    val folders: List<Folder> = emptyList(),
    val showCreateDialog: Boolean = false,
    val newFolderName: String = ""
)

sealed interface FoldersSheetUiEvent {
    data object ShowCreateDialog : FoldersSheetUiEvent
    data object DismissCreateDialog : FoldersSheetUiEvent
    data object CreateFolder : FoldersSheetUiEvent
    data class NewFolderNameChanged(val name: String) : FoldersSheetUiEvent
    data class DeleteFolder(val folder: Folder) : FoldersSheetUiEvent
}