package com.example.notes.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notes.core.model.AppColor
import com.example.notes.core.model.Folder

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: AppColor = AppColor.LAVENDER,
    val createdAt: Long = System.currentTimeMillis()
)

fun FolderEntity.toDomain(): Folder = Folder(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)

fun Folder.toEntity(): FolderEntity = FolderEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)