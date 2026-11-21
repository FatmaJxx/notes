package com.example.notes.core.data.repository

import com.example.notes.core.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    suspend fun getFolderById(id: Long): Folder?
    suspend fun getFolderNameById(id: Long): String?
    suspend fun createFolder(folder: Folder): Long
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folder: Folder)
}
