package com.example.notes.core.model

data class Folder(
    val id: Long = 0,
    val name: String,
    val color: AppColor = AppColor.LAVENDER,
    val createdAt: Long = System.currentTimeMillis()
)
