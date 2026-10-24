package com.example.cafetal87

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------
// PANTALLA 1: INICIO (bienvenida + favoritos)
// ---------------------------------------------------------------
@Composable
fun PantallaInicio(
    favoritos: List<Producto>,
    cantidades: Map<Int, Int>,
    onSumar: (Producto) -> Unit,
    onRestar: (Producto) -> Unit,
    onFavorito: (Producto) -> Unit,
    onIrAlMenu: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Banner(onIrAlMenu) }
        item { TituloSeccion("Tus favoritos ♥") }
        if (favoritos.isEmpty()) {
            item {
                Text(
                    "Aún no tienes favoritos. Toca el corazón de un producto en el menú y aparecerá aquí.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(favoritos, key = { it.id }) { producto ->
                TarjetaProducto(
                    producto = producto,
                    cantidad = cantidades[producto.id] ?: 0,
                    esFavorito = true,
                    onSumar = { onSumar(producto) },
                    onRestar = { onRestar(producto) },
                    onFavorito = { onFavorito(producto) }
                )
            }
        }
    }
}

@Composable
private fun Banner(onIrAlMenu: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("☕", fontSize = 40.sp)
            Text(
                "Cafetal87",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                "Café de nuestra tierra, preparado con cariño.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onIrAlMenu,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Ver el menú")
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA 2: MENÚ (lista de productos)
// ---------------------------------------------------------------
@Composable
fun PantallaMenu(
    favoritosIds: List<Int>,
    cantidades: Map<Int, Int>,
    onSumar: (Producto) -> Unit,
    onRestar: (Producto) -> Unit,
    onFavorito: (Producto) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TituloSeccion("Nuestro menú") }
        items(listaProductos, key = { it.id }) { producto ->
            TarjetaProducto(
                producto = producto,
                cantidad = cantidades[producto.id] ?: 0,
                esFavorito = producto.id in favoritosIds,
                onSumar = { onSumar(producto) },
                onRestar = { onRestar(producto) },
                onFavorito = { onFavorito(producto) }
            )
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA 3: MI PEDIDO (cantidades, personalización y total)
// ---------------------------------------------------------------
@Composable
fun PantallaPedido(
    cantidades: Map<Int, Int>,
    tamanos: Map<Int, Tamano>,
    leches: Map<Int, Leche>,
    onSumar: (Producto) -> Unit,
    onRestar: (Producto) -> Unit,
    onTamano: (Producto, Tamano) -> Unit,
    onLeche: (Producto, Leche) -> Unit,
    onConfirmar: () -> Unit,
    onIrAlMenu: () -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    val seleccionados = listaProductos.filter { (cantidades[it.id] ?: 0) > 0 }
    val total = seleccionados.sumOf { p ->
        (cantidades[p.id] ?: 0) * precioUnitario(
            p,
            tamanos[p.id] ?: Tamano.MEDIANO,
            leches[p.id] ?: Leche.ENTERA
        )
    }

    if (seleccionados.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🛒", fontSize = 56.sp)
            Text("Tu pedido está vacío", style = MaterialTheme.typography.titleLarge)
            Text(
                "Agrega algo rico desde el menú.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onIrAlMenu) { Text("Ir al menú") }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TituloSeccion("Resumen de tu pedido") }
            items(seleccionados, key = { it.id }) { producto ->
                LineaPedido(
                    producto = producto,
                    cantidad = cantidades[producto.id] ?: 0,
                    tamano = tamanos[producto.id] ?: Tamano.MEDIANO,
                    leche = leches[producto.id] ?: Leche.ENTERA,
                    onSumar = { onSumar(producto) },
                    onRestar = { onRestar(producto) },
                    onTamano = { onTamano(producto, it) },
                    onLeche = { onLeche(producto, it) }
                )
            }
            item { ResumenTotal(total = total, onConfirmar = { mostrarDialogo = true }) }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("¡Pedido confirmado! ☕") },
            text = { Text("Total a pagar: ${total.comoPrecio()}\nGracias por elegir Cafetal87.") },
            confirmButton = {
                TextButton(onClick = {
                    onConfirmar()
                    mostrarDialogo = false
                }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
private fun LineaPedido(
    producto: Producto,
    cantidad: Int,
    tamano: Tamano,
    leche: Leche,
    onSumar: () -> Unit,
    onRestar: () -> Unit,
    onTamano: (Tamano) -> Unit,
    onLeche: (Leche) -> Unit
) {
    val unitario = precioUnitario(producto, tamano, leche)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiCirculo(producto.emoji, 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("${unitario.comoPrecio()} c/u", style = MaterialTheme.typography.bodySmall)
                }
                ControlCantidad(cantidad, onSumar, onRestar)
            }

            // Personalización: solo para bebidas
            if (producto.categoria == CATEGORIA_BEBIDAS) {
                SelectorOpciones(
                    titulo = "Tamaño",
                    opciones = Tamano.values().toList(),
                    seleccionada = tamano,
                    etiqueta = { it.etiqueta },
                    onSeleccion = onTamano
                )
                SelectorOpciones(
                    titulo = "Tipo de leche",
                    opciones = Leche.values().toList(),
                    seleccionada = leche,
                    etiqueta = { it.etiqueta },
                    onSeleccion = onLeche
                )
            }

            Text(
                text = "Subtotal: ${(unitario * cantidad).comoPrecio()}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ResumenTotal(total: Int, onConfirmar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total", style = MaterialTheme.typography.bodySmall)
                Text(total.comoPrecio(), style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = onConfirmar) { Text("Confirmar pedido") }
        }
    }
}