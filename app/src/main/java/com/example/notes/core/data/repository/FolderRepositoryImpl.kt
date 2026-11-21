package com.example.notes.core.data.repository

import com.example.notes.core.database.dao.FolderDao
import com.example.notes.core.database.entity.toDomain
import com.example.notes.core.database.entity.toEntity
import kotlinx.coroutines.flow.Flow
import com.example.notes.core.model.Folder
import kotlinx.coroutines.flow.map


class FolderRepositoryImpl(
    private val folderDao: FolderDao
) : FolderRepository {
    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders().map { list ->
            list.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun getFolderById(id: Long): Folder? {
        return folderDao.getFolderById(id)?.toDomain()
    }

    override suspend fun getFolderNameById(id: Long): String? {
        return folderDao.getFolderNameById(id)
    }

    override suspend fun createFolder(folder: Folder): Long {
        return folderDao.insertFolder(folder.toEntity())
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(folder.toEntity())
    }

    override suspend fun deleteFolder(folder: Folder) {
        folderDao.deleteFolder(folder.toEntity())
    }
}
