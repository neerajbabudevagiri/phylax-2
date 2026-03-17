package com.example.phylaxfileaccess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phylaxfileaccess.data.FileRepository
import com.example.phylaxfileaccess.models.FileItem
import com.example.phylaxfileaccess.models.StorageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FileViewModel(private val repository: FileRepository) : ViewModel() {

    private val _recentFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val recentFiles: StateFlow<List<FileItem>> = _recentFiles.asStateFlow()

    private val _categoryFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val categoryFiles: StateFlow<List<FileItem>> = _categoryFiles.asStateFlow()

    private val _storage = MutableStateFlow<StorageInfo?>(null)
    val storage: StateFlow<StorageInfo?> = _storage.asStateFlow()

    fun loadFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _recentFiles.value = repository.getRecentFiles()
        }
    }

    fun loadFilesByCategory(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _categoryFiles.value = repository.getFilesByCategory(category)
        }
    }

    fun loadStorage() {
        viewModelScope.launch(Dispatchers.IO) {
            _storage.value = repository.getStorageInfo()
        }
    }
}
