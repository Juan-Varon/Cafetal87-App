package com.example.cafetal87.data

import android.content.Context
import com.example.cafetal87.data.local.AppDatabase
import com.example.cafetal87.data.local.DetallePedido
import com.example.cafetal87.data.local.Favorito
import com.example.cafetal87.data.local.Pedido
import com.example.cafetal87.data.local.PedidoConDetalles
import com.example.cafetal87.data.local.Producto
import com.example.cafetal87.data.local.Usuario
import kotlinx.coroutines.flow.Flow

// Repository: único punto de acceso a los datos. Los ViewModel no conocen Room directamente.
class CafeteriaRepository(db: AppDatabase) {
    private val usuarioDao = db.usuarioDao()
    private val productoDao = db.productoDao()
    private val pedidoDao = db.pedidoDao()
    private val favoritoDao = db.favoritoDao()

    // ---------- Usuarios ----------
    suspend fun buscarUsuario(correo: String): Usuario? = usuarioDao.buscarPorCorreo(correo)

    suspend fun registrarUsuario(usuario: Usuario) {
        usuarioDao.insertar(usuario)
    }

    // ---------- Productos (CRUD) ----------
    fun observarProductos(): Flow<List<Producto>> = productoDao.observarTodos()

    suspend fun insertarProducto(producto: Producto) {
        productoDao.insertar(producto)
    }

    suspend fun actualizarProducto(producto: Producto) {
        productoDao.actualizar(producto)
    }

    suspend fun eliminarProducto(producto: Producto) {
        productoDao.eliminar(producto)
    }

    // ---------- Favoritos (por usuario) ----------
    fun observarFavoritosIds(usuarioId: Int): Flow<List<Int>> = favoritoDao.observarIds(usuarioId)

    suspend fun alternarFavorito(usuarioId: Int, productoId: Int, eraFavorito: Boolean) {
        if (eraFavorito) {
            favoritoDao.quitar(usuarioId, productoId)
        } else {
            favoritoDao.agregar(Favorito(usuarioId, productoId))
        }
    }

    // ---------- Pedidos ----------
    fun observarTodosLosPedidos(): Flow<List<PedidoConDetalles>> = pedidoDao.observarTodos()

    fun observarPedidosDe(usuarioId: Int): Flow<List<PedidoConDetalles>> =
        pedidoDao.observarPorUsuario(usuarioId)

    suspend fun crearPedido(pedido: Pedido, detalles: List<DetallePedido>) {
        pedidoDao.crearPedidoCompleto(pedido, detalles)
    }

    suspend fun cambiarEstadoPedido(id: Int, estado: String) {
        pedidoDao.cambiarEstado(id, estado)
    }

    // ---------- Datos iniciales (solo si la base está vacía) ----------
    suspend fun sembrarDatosIniciales() {
        if (usuarioDao.contar() == 0) {
            usuarioDao.insertar(
                Usuario(
                    nombre = "Empleado Cafetal87",
                    correo = "empleado@cafetal87.com",
                    contrasenaHash = hashContrasena("empleado123"),
                    rol = Roles.EMPLEADO
                )
            )
            usuarioDao.insertar(
                Usuario(
                    nombre = "Cliente de prueba",
                    correo = "cliente@cafetal87.com",
                    contrasenaHash = hashContrasena("cliente123"),
                    rol = Roles.CLIENTE
                )
            )
        }
        if (productoDao.contar() == 0) {
            productoDao.insertarTodos(
                listOf(
                    Producto(nombre = "Tinto", descripcion = "Café colombiano, clásico y fuerte", precio = 2500, categoria = CATEGORIA_BEBIDAS, emoji = "☕"),
                    Producto(nombre = "Café americano", descripcion = "Espresso largo, intenso y suave", precio = 4000, categoria = CATEGORIA_BEBIDAS, emoji = "🫖"),
                    Producto(nombre = "Capuchino", descripcion = "Espresso con leche espumada", precio = 6500, categoria = CATEGORIA_BEBIDAS, emoji = "🥛"),
                    Producto(nombre = "Chocolate caliente", descripcion = "Con leche y un toque de canela", precio = 5500, categoria = CATEGORIA_BEBIDAS, emoji = "🍫"),
                    Producto(nombre = "Croissant", descripcion = "Horneado en el día, mantequilloso", precio = 4500, categoria = CATEGORIA_COMIDA, emoji = "🥐"),
                    Producto(nombre = "Torta de zanahoria", descripcion = "Porción con crema de queso", precio = 7000, categoria = CATEGORIA_COMIDA, emoji = "🍰"),
                    Producto(nombre = "Pan de bono", descripcion = "Caliente y esponjoso", precio = 3000, categoria = CATEGORIA_COMIDA, emoji = "🧀"),
                    Producto(nombre = "Sándwich de pollo", descripcion = "Pollo desmechado, queso y verduras", precio = 9000, categoria = CATEGORIA_COMIDA, emoji = "🥪")
                )
            )
        }
    }

    companion object {
        @Volatile
        private var instancia: CafeteriaRepository? = null

        fun obtener(context: Context): CafeteriaRepository {
            return instancia ?: synchronized(this) {
                instancia ?: CafeteriaRepository(AppDatabase.obtener(context)).also { instancia = it }
            }
        }
    }
}
