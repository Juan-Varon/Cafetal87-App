package com.example.cafetal87.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY categoria, nombre")
    fun observarTodos(): Flow<List<Producto>>

    @Insert
    suspend fun insertar(producto: Producto): Long

    @Insert
    suspend fun insertarTodos(productos: List<Producto>)

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun eliminar(producto: Producto)

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int
}

@Dao
interface FavoritoDao {
    @Query("SELECT productoId FROM favoritos WHERE usuarioId = :usuarioId")
    fun observarIds(usuarioId: Int): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregar(favorito: Favorito)

    @Query("DELETE FROM favoritos WHERE usuarioId = :usuarioId AND productoId = :productoId")
    suspend fun quitar(usuarioId: Int, productoId: Int)
}

@Dao
abstract class PedidoDao {
    @Insert
    abstract suspend fun insertarPedido(pedido: Pedido): Long

    @Insert
    abstract suspend fun insertarDetalles(detalles: List<DetallePedido>)

    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY fecha DESC")
    abstract fun observarTodos(): Flow<List<PedidoConDetalles>>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    abstract fun observarPorUsuario(usuarioId: Int): Flow<List<PedidoConDetalles>>

    @Query("UPDATE pedidos SET estado = :estado WHERE id = :id")
    abstract suspend fun cambiarEstado(id: Int, estado: String)

    // Guarda el pedido y todos sus detalles en una sola transacción
    @Transaction
    open suspend fun crearPedidoCompleto(pedido: Pedido, detalles: List<DetallePedido>) {
        val idPedido = insertarPedido(pedido).toInt()
        insertarDetalles(detalles.map { it.copy(pedidoId = idPedido) })
    }
}
