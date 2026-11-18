package com.example.cafetal87.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafetal87.data.CafeteriaRepository
import com.example.cafetal87.data.local.PedidoConDetalles
import com.example.cafetal87.data.local.Producto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Gestión para el empleado: CRUD de productos, pedidos de todos los clientes y estadísticas
class AdminViewModel(private val repo: CafeteriaRepository) : ViewModel() {

    val productos: StateFlow<List<Producto>> = repo.observarProductos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pedidos: StateFlow<List<PedidoConDetalles>> = repo.observarTodosLosPedidos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalVendido: StateFlow<Int> = pedidos
        .map { lista -> lista.sumOf { it.pedido.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Devuelve un mensaje de error si hay datos inválidos; si todo está bien, guarda y devuelve null
    fun guardarProducto(
        existente: Producto?,
        nombre: String,
        descripcion: String,
        precioTexto: String,
        categoria: String,
        emoji: String,
        disponible: Boolean,
        alGuardar: () -> Unit
    ): String? {
        if (nombre.isBlank()) return "El nombre es obligatorio"
        val precio = precioTexto.trim().toIntOrNull()
        if (precio == null || precio <= 0) return "Ingresa un precio válido mayor a 0"

        val emojiFinal = emoji.trim().ifEmpty { "☕" }
        viewModelScope.launch {
            if (existente == null) {
                repo.insertarProducto(
                    Producto(
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim(),
                        precio = precio,
                        categoria = categoria,
                        emoji = emojiFinal,
                        disponible = disponible
                    )
                )
            } else {
                repo.actualizarProducto(
                    existente.copy(
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim(),
                        precio = precio,
                        categoria = categoria,
                        emoji = emojiFinal,
                        disponible = disponible
                    )
                )
            }
            alGuardar()
        }
        return null
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch { repo.eliminarProducto(producto) }
    }

    fun alternarDisponible(producto: Producto) {
        viewModelScope.launch {
            repo.actualizarProducto(producto.copy(disponible = !producto.disponible))
        }
    }

    fun cambiarEstadoPedido(idPedido: Int, estado: String) {
        viewModelScope.launch { repo.cambiarEstadoPedido(idPedido, estado) }
    }
}
