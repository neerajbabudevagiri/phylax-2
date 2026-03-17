package com.example.phylaxfileaccess.data

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.example.phylaxfileaccess.models.FileItem
import com.example.phylaxfileaccess.models.StorageInfo

class FileRepository(private val context: Context) {

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

    fun getFilesByCategory(category: String): List<FileItem> {
        val files = mutableListOf<FileItem>()
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE
        )

        val selection = when (category.lowercase()) {
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
            else -> null
        }

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
}
