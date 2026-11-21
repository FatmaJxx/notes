package com.example.notes.feature.folders.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.R
import com.example.notes.core.designsystem.components.PulseAnimation
import com.example.notes.core.designsystem.theme.notes.CardShape
import com.example.notes.core.designsystem.theme.notes.DialogShape
import com.example.notes.core.designsystem.theme.notes.PillShape
import com.example.notes.core.model.AppColor
import com.example.notes.feature.folders.details.FolderType
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    modifier: Modifier = Modifier,
    viewModel: FoldersViewModel = koinViewModel(),
    onFolderClick: (FolderType) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is FoldersNavigationEvents.NavigateToFolderDetails -> onFolderClick(event.folder)
            }
        }
    }
    val folderState by viewModel.folderState.collectAsStateWithLifecycle()
    val newFolderName = viewModel.newFolderName

    FoldersScreen(
        newFolderName = newFolderName,
        folderState = folderState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    newFolderName: String,
    folderState: FoldersUiState,
    onEvent: (FoldersUiEvent) -> Unit,
    modifier: Modifier
) {

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.feature_folders_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { onEvent(FoldersUiEvent.ToggleDialog) }) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (folderState.isLoading) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PulseAnimation(
                    circleColor = MaterialTheme.colorScheme.primary,
                    circleSize = 16.dp
                )
            }
        }else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    FolderRowItem(
                        title = stringResource(R.string.feature_folders_pinned_folder_title),
                        count = "${folderState.pinnedCount} notes",
                        icon = Icons.Filled.PushPin,
                        iconBgColor = Color(0xFFEAE4F8),
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { onEvent(FoldersUiEvent.OpenFolder(FolderType.Pinned)) }
                    )
                }
                item {
                    FolderRowItem(
                        title = stringResource(R.string.feature_folders_favorites_folder_title),
                        count = "${folderState.favoritesCount} notes",
                        icon = Icons.Filled.Star,
                        iconBgColor = Color(0xFFFBE8EE),
                        iconTint = Color(0xFFE07A9E),
                        onClick = { onEvent(FoldersUiEvent.OpenFolder(FolderType.Favorites)) }
                    )
                }

                items(folderState.folders, key = { it.folder.id }) { item ->
                    val folderColor =
                        item.folder.color.get()

                    FolderRowItem(
                        title = item.folder.name,
                        count = "${item.count} notes",
                        icon = Icons.Filled.Folder,
                        iconBgColor = folderColor.copy(alpha = 0.2f),
                        iconTint = folderColor,
                        onClick = {
                            onEvent(
                                FoldersUiEvent.OpenFolder(
                                    FolderType.Folder(
                                        item.folder.id,
                                        item.folder.name
                                    )
                                )
                            )
                        }
                    )
                }

                item {
                    FolderRowItem(
                        title = stringResource(R.string.feature_folders_tags_folder_title),
                        count = "Organize tags",
                        icon = Icons.AutoMirrored.Filled.Label,
                        iconBgColor = Color(0xFFE7F4EB),
                        iconTint = Color(0xFF5DBB7D),
                        onClick = { onEvent(FoldersUiEvent.OpenFolder(FolderType.Tags)) }
                    )
                }

                item {
                    FolderRowItem(
                        title = stringResource(R.string.feature_folders_archive_folder_title),
                        count = "${folderState.archivedCount} notes",
                        icon = Icons.Filled.Archive,
                        iconBgColor = Color(0xFFFFF8D8),
                        iconTint = Color(0xFFE5B53B),
                        onClick = { onEvent(FoldersUiEvent.OpenFolder(FolderType.Archive)) }
                    )
                }

                item {
                    FolderRowItem(
                        title = stringResource(R.string.feature_folders_trash_folder_title),
                        count = "${folderState.trashCount} notes",
                        icon = Icons.Filled.Delete,
                        iconBgColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { onEvent(FoldersUiEvent.OpenFolder(FolderType.Trash)) }
                    )
                }
            }
        }
    }

    if (folderState.showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                onEvent(FoldersUiEvent.ToggleDialog)
            },
            shape = DialogShape,
            title = { Text(stringResource(R.string.feature_folders_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = {
                        onEvent(FoldersUiEvent.FolderNameChange(it))
                    },
                    label = { Text(stringResource(R.string.feature_folders_text_field_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onEvent(FoldersUiEvent.CreateFolder(newFolderName, AppColor.LAVENDER))
                        }
                    },
                    shape = PillShape
                ) {
                    Text(stringResource(R.string.feature_folders_btn_action_create))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onEvent(FoldersUiEvent.ToggleDialog)
                }
                ) {
                    Text(stringResource(R.string.feature_folders_btn_action_cancel))
                }
            }
        )
    }
}

@Composable
fun FolderRowItem(
    title: String,
    count: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconBgColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = count,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
