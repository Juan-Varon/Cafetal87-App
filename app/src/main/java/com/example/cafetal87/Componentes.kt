package com.example.cafetal87

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Convierte 6500 en "$6.500"
fun Int.comoPrecio(): String = "\$" + "%,d".format(this).replace(",", ".")

// Composable reutilizable 1: círculo con el emoji del producto
@Composable
fun EmojiCirculo(emoji: String, tamano: Dp = 56.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = (tamano.value / 2).sp)
    }
}

// Composable reutilizable 2: título de sección
@Composable
fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

// Composable reutilizable 3: botones - cantidad +
@Composable
fun ControlCantidad(cantidad: Int, onSumar: () -> Unit, onRestar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedIconButton(onClick = onRestar) { Text("-") }
        Text(
            text = "$cantidad",
            modifier = Modifier.padding(horizontal = 10.dp),
            style = MaterialTheme.typography.titleMedium
        )
        FilledIconButton(onClick = onSumar) { Text("+") }
    }
}

// Composable reutilizable 4: tarjeta de producto (con favorito y cantidad)
@Composable
fun TarjetaProducto(
    producto: Producto,
    cantidad: Int,
    esFavorito: Boolean,
    onSumar: () -> Unit,
    onRestar: () -> Unit,
    onFavorito: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmojiCirculo(producto.emoji)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                Text(producto.descripcion, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = producto.precio.comoPrecio(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onFavorito) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Marcar como favorito",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                ControlCantidad(cantidad, onSumar, onRestar)
            }
        }
    }
}

// Composable reutilizable 5: selector de opciones con chips (sirve para tamaño, leche, etc.)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorOpciones(
    titulo: String,
    opciones: List<T>,
    seleccionada: T,
    etiqueta: (T) -> String,
    onSeleccion: (T) -> Unit
) {
    Column {
        Text(titulo, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            opciones.forEach { opcion ->
                FilterChip(
                    selected = opcion == seleccionada,
                    onClick = { onSeleccion(opcion) },
                    label = { Text(etiqueta(opcion)) }
                )
            }
        }
    }
}