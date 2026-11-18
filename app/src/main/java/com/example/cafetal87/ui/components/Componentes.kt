package com.example.cafetal87.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafetal87.data.Estados
import com.example.cafetal87.data.local.PedidoConDetalles
import com.example.cafetal87.data.local.Producto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Convierte 6500 en "$6.500"
fun Int.comoPrecio(): String = "\$" + "%,d".format(this).replace(",", ".")

// Convierte milisegundos en una fecha legible
fun Long.comoFecha(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(this))

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
fun ControlCantidad(
    cantidad: Int,
    onSumar: () -> Unit,
    onRestar: () -> Unit,
    sumarHabilitado: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedIconButton(onClick = onRestar) { Text("-") }
        Text(
            text = "$cantidad",
            modifier = Modifier.padding(horizontal = 10.dp),
            style = MaterialTheme.typography.titleMedium
        )
        FilledIconButton(onClick = onSumar, enabled = sumarHabilitado) { Text("+") }
    }
}

// Composable reutilizable 4: tarjeta de producto con favorito y cantidad
@Composable
fun TarjetaProducto(
    producto: Producto,
    esFavorito: Boolean,
    cantidad: Int,
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
                if (!producto.disponible) {
                    Text(
                        text = "Agotado",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onFavorito) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Marcar como favorito",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                ControlCantidad(
                    cantidad = cantidad,
                    onSumar = onSumar,
                    onRestar = onRestar,
                    sumarHabilitado = producto.disponible
                )
            }
        }
    }
}

// Composable reutilizable 5: selector de opciones con chips (sirve para cualquier lista)
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
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

// Composable reutilizable 6: tarjeta de un pedido (la usan cliente y empleado)
@Composable
fun TarjetaPedido(
    item: PedidoConDetalles,
    mostrarCliente: Boolean,
    onCambiarEstado: ((String) -> Unit)? = null
) {
    val colorEstado = when (item.pedido.estado) {
        Estados.PENDIENTE -> MaterialTheme.colorScheme.tertiary
        Estados.PREPARANDO -> MaterialTheme.colorScheme.secondary
        else -> Color(0xFF2E7D32)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pedido #${item.pedido.id}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.pedido.estado,
                    style = MaterialTheme.typography.labelLarge,
                    color = colorEstado
                )
            }
            Text(item.pedido.fecha.comoFecha(), style = MaterialTheme.typography.bodySmall)
            if (mostrarCliente) {
                Text("Cliente: ${item.usuario.nombre}", style = MaterialTheme.typography.bodySmall)
            }
            item.detalles.forEach { d ->
                val extra = if (d.tamano == "-") "" else " (${d.tamano}, leche ${d.leche.lowercase()})"
                Text(
                    text = "${d.emoji} ${d.nombreProducto} x${d.cantidad}$extra",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Total: ${item.pedido.total.comoPrecio()}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (onCambiarEstado != null) {
                SelectorOpciones(
                    titulo = "Estado del pedido",
                    opciones = Estados.todos,
                    seleccionada = item.pedido.estado,
                    etiqueta = { it },
                    onSeleccion = onCambiarEstado
                )
            }
        }
    }
}
