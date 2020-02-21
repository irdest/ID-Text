package com.example.id_text.bridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.id_text.data.TextRepository


class MainModelFactory(private val repository: TextRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }

}


