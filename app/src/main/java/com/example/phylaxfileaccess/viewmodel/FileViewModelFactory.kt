package com.example.phylaxfileaccess.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.phylaxfileaccess.data.FileRepository

class FileViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(FileViewModel::class.java)) {

            val repository = FileRepository(context)

            return FileViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}