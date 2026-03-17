package com.example.phylaxfileaccess.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getFileIcon(extension: String): ImageVector {

    return when(extension.lowercase()) {

        "jpg","jpeg","png","gif","webp" -> Icons.Default.Image

        "mp4","mkv","avi" -> Icons.Default.PlayArrow

        "mp3","wav","aac" -> Icons.Default.Audiotrack

        "pdf","doc","docx","txt" -> Icons.Default.Description

        "zip","rar","7z" -> Icons.Default.FolderZip

        "apk" -> Icons.Default.Android

        else -> Icons.Default.InsertDriveFile
    }
}