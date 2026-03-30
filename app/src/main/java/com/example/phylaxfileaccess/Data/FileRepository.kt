package com.example.phylaxfileaccess.data

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.example.phylaxfileaccess.models.CategoryInfo
import com.example.phylaxfileaccess.models.FileActivityEvent
import com.example.phylaxfileaccess.models.FileItem
import com.example.phylaxfileaccess.models.StorageInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class FileRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("phylax_activity_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getRecentFiles(): List<FileItem> {
        val files = mutableListOf<FileItem>()
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: continue
                val path = it.getString(pathIndex) ?: continue
                val size = it.getLong(sizeIndex)

                files.add(
                    FileItem(
                        name = name,
                        path = path,
                        size = size,
                        extension = name.substringAfterLast(".", ""),
                        isDirectory = false
                    )
                )

                if (files.size >= 20) break
            }
        }
        return files
    }

    private fun getCategorySelection(category: String): String? {
        return when (category.lowercase()) {
            "images" -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"
            "videos" -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"
            "audio" -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO}"
            "documents" -> {
                val extensions = listOf("pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx")
                extensions.joinToString(prefix = "(", postfix = ")", separator = " OR ") { 
                    "${MediaStore.Files.FileColumns.DATA} LIKE '%.${it}'" 
                }
            }
            "apk files" -> "${MediaStore.Files.FileColumns.DATA} LIKE '%.apk'"
            "archives" -> {
                val extensions = listOf("zip", "rar", "7z", "tar", "gz")
                extensions.joinToString(prefix = "(", postfix = ")", separator = " OR ") { 
                    "${MediaStore.Files.FileColumns.DATA} LIKE '%.${it}'" 
                }
            }
            "recent files", "all files" -> null
            else -> "${MediaStore.Files.FileColumns._ID} = -1" // Force empty for custom categories
        }
    }

    fun getFilesByCategory(category: String): List<FileItem> {
        val files = mutableListOf<FileItem>()
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE
        )

        val selection = getCategorySelection(category)

        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: continue
                val path = it.getString(pathIndex) ?: continue
                val size = it.getLong(sizeIndex)

                files.add(
                    FileItem(
                        name = name,
                        path = path,
                        size = size,
                        extension = name.substringAfterLast(".", ""),
                        isDirectory = false
                    )
                )
            }
        }
        return files
    }

    fun searchFiles(query: String): List<FileItem> {
        val files = mutableListOf<FileItem>()
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE
        )
        
        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")

        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: continue
                val path = it.getString(pathIndex) ?: continue
                val size = it.getLong(sizeIndex)

                files.add(
                    FileItem(
                        name = name,
                        path = path,
                        size = size,
                        extension = name.substringAfterLast(".", ""),
                        isDirectory = false
                    )
                )
                if (files.size >= 50) break
            }
        }
        return files
    }

    fun getCategoryInfo(category: String): CategoryInfo {
        val uri = MediaStore.Files.getContentUri("external")
        // Only select columns that are guaranteed to exist. Aggregate functions are not standard in MediaStore projection.
        val projection = arrayOf(
            MediaStore.Files.FileColumns.SIZE
        )
        val selection = getCategorySelection(category)

        var count = 0
        var totalSize = 0L

        try {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                null
            )?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                while (cursor.moveToNext()) {
                    count++
                    if (sizeIndex != -1) {
                        totalSize += cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            // Log or handle error - returning zeroed info is safer than crashing
        }

        return CategoryInfo(category, count, totalSize)
    }

    fun getStorageInfo(): StorageInfo {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val total = totalBlocks * blockSize
        val free = availableBlocks * blockSize
        val used = total - free

        return StorageInfo(
            totalSpace = total,
            usedSpace = used,
            freeSpace = free
        )
    }

    fun logActivityEvent(event: FileActivityEvent) {
        val events = getActivityEvents(event.filePath).toMutableList()
        events.add(event)
        prefs.edit().putString(event.filePath, gson.toJson(events)).apply()
    }

    fun getActivityEvents(filePath: String): List<FileActivityEvent> {
        val json = prefs.getString(filePath, null) ?: return emptyList()
        val type = object : TypeToken<List<FileActivityEvent>>() {}.type
        return gson.fromJson(json, type)
    }
}
