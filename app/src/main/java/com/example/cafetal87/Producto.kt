package com.example.cafetal87

const val CATEGORIA_BEBIDAS = "Bebidas"
const val CATEGORIA_COMIDA = "Comida"

// Data class: representa la información principal del negocio (un producto del menú)
data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val categoria: String,
    val emoji: String
)

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

// Colección con los productos (datos locales, sin base de datos)
val listaProductos = listOf(
    Producto(1, "Tinto", "Café colombiano, clásico y fuerte", 2500, CATEGORIA_BEBIDAS, "☕"),
    Producto(2, "Café americano", "Espresso largo, intenso y suave", 4000, CATEGORIA_BEBIDAS, "🫖"),
    Producto(3, "Capuchino", "Espresso con leche espumada", 6500, CATEGORIA_BEBIDAS, "🥛"),
    Producto(4, "Chocolate caliente", "Con leche y un toque de canela", 5500, CATEGORIA_BEBIDAS, "🍫"),
    Producto(5, "Croissant", "Horneado en el día, mantequilloso", 4500, CATEGORIA_COMIDA, "🥐"),
    Producto(6, "Torta de zanahoria", "Porción con crema de queso", 7000, CATEGORIA_COMIDA, "🍰"),
    Producto(7, "Pan de bono", "Caliente y esponjoso", 3000, CATEGORIA_COMIDA, "🧀"),
    Producto(8, "Sándwich de pollo", "Pollo desmechado, queso y verduras", 9000, CATEGORIA_COMIDA, "🥪")
)

// Precio de una unidad: las bebidas cambian según el tamaño y la leche elegidos
fun precioUnitario(producto: Producto, tamano: Tamano, leche: Leche): Int {
    return if (producto.categoria == CATEGORIA_BEBIDAS) {
        producto.precio + tamano.extra + leche.extra
    } else {
        producto.precio
    }
}