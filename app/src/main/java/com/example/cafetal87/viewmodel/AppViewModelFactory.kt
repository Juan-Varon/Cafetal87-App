package com.example.cafetal87.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cafetal87.data.CafeteriaRepository

// Crea los ViewModel pasándoles el Repository
class AppViewModelFactory(private val repo: CafeteriaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AuthViewModel::class.java -> AuthViewModel(repo) as T
            MenuViewModel::class.java -> MenuViewModel(repo) as T
            PedidoViewModel::class.java -> PedidoViewModel(repo) as T
            AdminViewModel::class.java -> AdminViewModel(repo) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
