package com.example.notes.core.agent.chat.data

import android.content.Context

fun modelPath(context: Context) = ModelPath(
    modelDir = context.noBackupFilesDir.resolve("models").absolutePath,
    cacheDir = context.cacheDir.resolve("litertlm").apply { mkdirs() }.absolutePath
)

data class ModelPath(
    val modelDir: String,
    val cacheDir: String
){
    val modelFile: String get () = "$modelDir/${LocalModel.FILE_NAME}"
}