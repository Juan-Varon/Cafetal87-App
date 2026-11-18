package com.example.cafetal87.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// Entidad 1: usuarios (clientes y empleados)
@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val contrasenaHash: String,
    val rol: String
)

// Entidad 2: productos del menú (tiene CRUD completo)
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val categoria: String,
    val emoji: String,
    val disponible: Boolean = true
)

// Entidad 3: pedidos. Cada pedido pertenece a un usuario (relación 1 a muchos)
@Entity(
    tableName = "pedidos",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class Pedido(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val fecha: Long,
    val total: Int,
    val estado: String
)

// Entidad 4: líneas de cada pedido (un pedido tiene varios detalles)
@Entity(
    tableName = "detalles_pedido",
    foreignKeys = [
        ForeignKey(
            entity = Pedido::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pedidoId")]
)
data class DetallePedido(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pedidoId: Int,
    val productoId: Int,
    val nombreProducto: String,
    val emoji: String,
    val cantidad: Int,
    val tamano: String,
    val leche: String,
    val precioUnitario: Int
)

// Entidad 5: favoritos por usuario (relación muchos a muchos entre Usuario y Producto)
@Entity(
    tableName = "favoritos",
    primaryKeys = ["usuarioId", "productoId"],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productoId")]
)
data class Favorito(
    val usuarioId: Int,
    val productoId: Int
)

// Resultado de consulta: un pedido junto con sus detalles y su usuario
data class PedidoConDetalles(
    @Embedded val pedido: Pedido,
    @Relation(parentColumn = "id", entityColumn = "pedidoId")
    val detalles: List<DetallePedido>,
    @Relation(parentColumn = "usuarioId", entityColumn = "id")
    val usuario: Usuario
)
