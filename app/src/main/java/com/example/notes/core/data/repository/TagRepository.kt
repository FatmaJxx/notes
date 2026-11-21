package com.example.notes.core.data.repository

import com.example.notes.core.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun createTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
}