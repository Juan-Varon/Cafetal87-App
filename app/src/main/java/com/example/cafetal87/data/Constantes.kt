package com.example.cafetal87.data

import com.example.cafetal87.data.local.Producto
import java.security.MessageDigest

object Roles {
    const val CLIENTE = "CLIENTE"
    const val EMPLEADO = "EMPLEADO"
}

object Estados {
    const val PENDIENTE = "Pendiente"
    const val PREPARANDO = "Preparando"
    const val ENTREGADO = "Entregado"
    val todos = listOf(PENDIENTE, PREPARANDO, ENTREGADO)
}

// Código que debe ingresar quien se registre como empleado
const val CODIGO_EMPLEADO = "CAFE87"

const val CATEGORIA_BEBIDAS = "Bebidas"
const val CATEGORIA_COMIDA = "Comida"
val CATEGORIAS = listOf(CATEGORIA_BEBIDAS, CATEGORIA_COMIDA)

// Opciones para personalizar una bebida (cada una suma o resta al precio)
enum class Tamano(val etiqueta: String, val extra: Int) {
    PEQUENO("Pequeño", -1000),
    MEDIANO("Mediano", 0),
    GRANDE("Grande", 1500)
}

enum class Leche(val etiqueta: String, val extra: Int) {
    ENTERA("Entera", 0),
    SIN_LACTOSA("Sin lactosa", 0),
    ALMENDRA("Almendra", 1000)
}

// Precio de una unidad: las bebidas cambian según el tamaño y la leche elegidos
fun precioUnitario(producto: Producto, tamano: Tamano, leche: Leche): Int {
    val precio = if (producto.categoria == CATEGORIA_BEBIDAS) {
        producto.precio + tamano.extra + leche.extra
    } else {
        producto.precio
    }
    return precio.coerceAtLeast(0)
}

// La contraseña nunca se guarda en texto plano: se guarda su hash SHA-256
fun hashContrasena(texto: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(texto.toByteArray())
        .joinToString("") { "%02x".format(it) }
