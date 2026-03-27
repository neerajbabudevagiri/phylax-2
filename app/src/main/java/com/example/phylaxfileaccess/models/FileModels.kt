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

data class FileActivityEvent(
    val id: String,
    val filePath: String,
    val timestamp: Long,
    val eventType: String, // "OPEN_SHARE", "FILE_SHARED"
    val targetApp: String? = null,
    val targetPackage: String? = null
)
