package com.example.phylaxfileaccess.models

import android.graphics.drawable.Drawable

data class FileItem(
    val name: String,
    val path: String,
    val size: Long,
    val extension: String,
    val isDirectory: Boolean
)

data class CategoryInfo(
    val name: String,
    val count: Int,
    val size: Long
)

data class SharingAppInfo(
    val name: String,
    val icon: Drawable,
    val packageName: String,
    val activityName: String
)
