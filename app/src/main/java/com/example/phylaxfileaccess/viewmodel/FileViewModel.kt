package com.example.phylaxfileaccess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phylaxfileaccess.data.FileRepository
import com.example.phylaxfileaccess.models.FileActivityEvent
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

    private val _activityEvents = MutableStateFlow<List<FileActivityEvent>>(emptyList())
    val activityEvents: StateFlow<List<FileActivityEvent>> = _activityEvents.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FileItem>>(emptyList())
    val searchResults: StateFlow<List<FileItem>> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    fun logActivity(event: FileActivityEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logActivityEvent(event)
        }
    }

    fun loadActivityEvents(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _activityEvents.value = repository.getActivityEvents(filePath)
        }
    }

    fun searchFiles(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _searchResults.value = repository.searchFiles(query)
        }
    }
}
