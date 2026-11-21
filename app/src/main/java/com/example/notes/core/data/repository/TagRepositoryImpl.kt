package com.example.notes.core.data.repository

import com.example.notes.core.database.dao.TagDao
import com.example.notes.core.database.entity.toDomain
import com.example.notes.core.database.entity.toEntity
import com.example.notes.core.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagRepositoryImpl(
    private val tagDao: TagDao
) : TagRepository {
    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createTag(tag: Tag): Long {
        val name = tag.name.trim()
        require(name.isNotEmpty()) { "Tag name must not be blank" }

        tagDao.getTagByName(name)?.let { return it.id }

        val id = tagDao.insertTag(Tag(name = name).toEntity())
        return if (id != -1L) id else checkNotNull(tagDao.getTagByName(name)).id
    }

    override suspend fun updateTag(tag: Tag) {
        tagDao.updateTag(tag.toEntity())
    }

    override suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(tag.toEntity())
    }
}