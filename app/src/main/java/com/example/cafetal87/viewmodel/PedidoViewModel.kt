package com.example.cafetal87.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafetal87.data.CATEGORIA_BEBIDAS
import com.example.cafetal87.data.CafeteriaRepository
import com.example.cafetal87.data.Estados
import com.example.cafetal87.data.Leche
import com.example.cafetal87.data.Tamano
import com.example.cafetal87.data.local.DetallePedido
import com.example.cafetal87.data.local.Pedido
import com.example.cafetal87.data.local.PedidoConDetalles
import com.example.cafetal87.data.local.Producto
import com.example.cafetal87.data.precioUnitario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Una línea del carrito: producto + cantidad + opciones elegidas
data class LineaCarrito(
    val producto: Producto,
    val cantidad: Int,
    val tamano: Tamano,
    val leche: Leche
) {
    val unitario: Int get() = precioUnitario(producto, tamano, leche)
    val subtotal: Int get() = unitario * cantidad
}

// Carrito (en memoria) + confirmación del pedido (se guarda en Room) + historial del cliente
class PedidoViewModel(private val repo: CafeteriaRepository) : ViewModel() {

    private val _cantidades = MutableStateFlow<Map<Int, Int>>(emptyMap())
    private val _tamanos = MutableStateFlow<Map<Int, Tamano>>(emptyMap())
    private val _leches = MutableStateFlow<Map<Int, Leche>>(emptyMap())

    val cantidades: StateFlow<Map<Int, Int>> = _cantidades.asStateFlow()

    private val productos: StateFlow<List<Producto>> = repo.observarProductos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lineas: StateFlow<List<LineaCarrito>> =
        combine(productos, _cantidades, _tamanos, _leches) { lista, cant, tam, lec ->
            lista.filter { (cant[it.id] ?: 0) > 0 }.map {
                LineaCarrito(
                    producto = it,
                    cantidad = cant[it.id] ?: 0,
                    tamano = tam[it.id] ?: Tamano.MEDIANO,
                    leche = lec[it.id] ?: Leche.ENTERA
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val total: StateFlow<Int> = lineas
        .map { l -> l.sumOf { it.subtotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalItems: StateFlow<Int> = _cantidades
        .map { it.values.sum() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun sumar(producto: Producto) {
        if (!producto.disponible) return
        _cantidades.update { it + (producto.id to ((it[producto.id] ?: 0) + 1)) }
    }

    fun restar(producto: Producto) {
        _cantidades.update {
            val actual = it[producto.id] ?: 0
            if (actual <= 1) it - producto.id else it + (producto.id to (actual - 1))
        }
    }

    fun cambiarTamano(productoId: Int, tamano: Tamano) {
        _tamanos.update { it + (productoId to tamano) }
    }

    fun cambiarLeche(productoId: Int, leche: Leche) {
        _leches.update { it + (productoId to leche) }
    }

    fun vaciar() {
        _cantidades.value = emptyMap()
        _tamanos.value = emptyMap()
        _leches.value = emptyMap()
    }

    // Guarda el pedido y sus detalles en Room
    fun confirmarPedido(usuarioId: Int, alTerminar: () -> Unit) {
        val items = lineas.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            val pedido = Pedido(
                usuarioId = usuarioId,
                fecha = System.currentTimeMillis(),
                total = items.sumOf { it.subtotal },
                estado = Estados.PENDIENTE
            )
            val detalles = items.map {
                val esBebida = it.producto.categoria == CATEGORIA_BEBIDAS
                DetallePedido(
                    pedidoId = 0,
                    productoId = it.producto.id,
                    nombreProducto = it.producto.nombre,
                    emoji = it.producto.emoji,
                    cantidad = it.cantidad,
                    tamano = if (esBebida) it.tamano.etiqueta else "-",
                    leche = if (esBebida) it.leche.etiqueta else "-",
                    precioUnitario = it.unitario
                )
            }
            repo.crearPedido(pedido, detalles)
            vaciar()
            alTerminar()
        }
    }

    // Historial de pedidos de un cliente
    fun historial(usuarioId: Int): Flow<List<PedidoConDetalles>> = repo.observarPedidosDe(usuarioId)
}
