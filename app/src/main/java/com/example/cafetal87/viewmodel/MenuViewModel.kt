package com.example.cafetal87.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafetal87.data.CafeteriaRepository
import com.example.cafetal87.data.local.Producto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Catálogo para el cliente: lista, búsqueda, filtro por categoría y favoritos (por usuario)
@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModel(private val repo: CafeteriaRepository) : ViewModel() {

    val productos: StateFlow<List<Producto>> = repo.observarProductos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _categoria = MutableStateFlow<String?>(null)
    val categoria: StateFlow<String?> = _categoria.asStateFlow()

    // Usuario que tiene la sesión iniciada (sus favoritos son los que se muestran)
    private val _usuarioId = MutableStateFlow<Int?>(null)

    val favoritosIds: StateFlow<Set<Int>> = _usuarioId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList<Int>()) else repo.observarFavoritosIds(id)
        }
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val productosFiltrados: StateFlow<List<Producto>> =
        combine(productos, _busqueda, _categoria) { lista, texto, cat ->
            lista.filter { p ->
                (cat == null || p.categoria == cat) &&
                    (texto.isBlank() || p.nombre.contains(texto.trim(), ignoreCase = true))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritos: StateFlow<List<Producto>> =
        combine(productos, favoritosIds) { lista, ids -> lista.filter { it.id in ids } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun establecerUsuario(id: Int?) {
        _usuarioId.value = id
    }

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun cambiarCategoria(categoria: String?) {
        _categoria.value = categoria
    }

    fun alternarFavorito(producto: Producto) {
        val usuarioId = _usuarioId.value ?: return
        val eraFavorito = producto.id in favoritosIds.value
        viewModelScope.launch {
            repo.alternarFavorito(usuarioId, producto.id, eraFavorito)
        }
    }
}
