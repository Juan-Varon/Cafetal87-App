package com.example.cafetal87.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafetal87.data.CATEGORIA_BEBIDAS
import com.example.cafetal87.data.CATEGORIA_COMIDA
import com.example.cafetal87.data.Leche
import com.example.cafetal87.data.Tamano
import com.example.cafetal87.data.local.Usuario
import com.example.cafetal87.ui.components.ControlCantidad
import com.example.cafetal87.ui.components.EmojiCirculo
import com.example.cafetal87.ui.components.SelectorOpciones
import com.example.cafetal87.ui.components.TarjetaPedido
import com.example.cafetal87.ui.components.TarjetaProducto
import com.example.cafetal87.ui.components.TituloSeccion
import com.example.cafetal87.ui.components.comoPrecio
import com.example.cafetal87.viewmodel.LineaCarrito
import com.example.cafetal87.viewmodel.MenuViewModel
import com.example.cafetal87.viewmodel.PedidoViewModel

// ---------------------------------------------------------------
// CLIENTE - INICIO (bienvenida + favoritos guardados en Room)
// ---------------------------------------------------------------
@Composable
fun PantallaInicio(
    usuario: Usuario,
    menuVm: MenuViewModel,
    pedidoVm: PedidoViewModel,
    onIrAlMenu: () -> Unit
) {
    val favoritos by menuVm.favoritos.collectAsState()
    val cantidades by pedidoVm.cantidades.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Banner(usuario.nombre.substringBefore(" "), onIrAlMenu) }
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
                    esFavorito = true,
                    cantidad = cantidades[producto.id] ?: 0,
                    onSumar = { pedidoVm.sumar(producto) },
                    onRestar = { pedidoVm.restar(producto) },
                    onFavorito = { menuVm.alternarFavorito(producto) }
                )
            }
        }
    }
}

@Composable
private fun Banner(nombre: String, onIrAlMenu: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("☕", fontSize = 40.sp)
            Text(
                "Hola, $nombre",
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
// CLIENTE - MENÚ (lista desde Room, búsqueda y filtro por categoría)
// ---------------------------------------------------------------
@Composable
fun PantallaMenu(menuVm: MenuViewModel, pedidoVm: PedidoViewModel) {
    val productos by menuVm.productosFiltrados.collectAsState()
    val busqueda by menuVm.busqueda.collectAsState()
    val categoria by menuVm.categoria.collectAsState()
    val favoritosIds by menuVm.favoritosIds.collectAsState()
    val cantidades by pedidoVm.cantidades.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TituloSeccion("Nuestro menú") }
        item {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { menuVm.cambiarBusqueda(it) },
                label = { Text("Buscar producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SelectorOpciones<String?>(
                titulo = "Categoría",
                opciones = listOf(null, CATEGORIA_BEBIDAS, CATEGORIA_COMIDA),
                seleccionada = categoria,
                etiqueta = { it ?: "Todas" },
                onSeleccion = { menuVm.cambiarCategoria(it) }
            )
        }
        if (productos.isEmpty()) {
            item { Text("No se encontraron productos.", style = MaterialTheme.typography.bodyLarge) }
        } else {
            items(productos, key = { it.id }) { producto ->
                TarjetaProducto(
                    producto = producto,
                    esFavorito = producto.id in favoritosIds,
                    cantidad = cantidades[producto.id] ?: 0,
                    onSumar = { pedidoVm.sumar(producto) },
                    onRestar = { pedidoVm.restar(producto) },
                    onFavorito = { menuVm.alternarFavorito(producto) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// CLIENTE - MI PEDIDO (carrito, personalización y confirmación en Room)
// ---------------------------------------------------------------
@Composable
fun PantallaPedido(
    usuario: Usuario,
    pedidoVm: PedidoViewModel,
    onIrAlMenu: () -> Unit,
    onIrAMisPedidos: () -> Unit
) {
    val lineas by pedidoVm.lineas.collectAsState()
    val total by pedidoVm.total.collectAsState()
    var mostrarExito by remember { mutableStateOf(false) }

    if (lineas.isEmpty()) {
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
            items(lineas, key = { it.producto.id }) { linea ->
                LineaPedido(
                    linea = linea,
                    onSumar = { pedidoVm.sumar(linea.producto) },
                    onRestar = { pedidoVm.restar(linea.producto) },
                    onTamano = { pedidoVm.cambiarTamano(linea.producto.id, it) },
                    onLeche = { pedidoVm.cambiarLeche(linea.producto.id, it) }
                )
            }
            item {
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
                        Button(onClick = {
                            pedidoVm.confirmarPedido(usuario.id) { mostrarExito = true }
                        }) {
                            Text("Confirmar pedido")
                        }
                    }
                }
            }
        }
    }

    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = { mostrarExito = false },
            title = { Text("¡Pedido registrado! ☕") },
            text = { Text("Tu pedido quedó guardado. Puedes ver su estado en Mis pedidos.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarExito = false
                    onIrAMisPedidos()
                }) {
                    Text("Ver mis pedidos")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarExito = false }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
private fun LineaPedido(
    linea: LineaCarrito,
    onSumar: () -> Unit,
    onRestar: () -> Unit,
    onTamano: (Tamano) -> Unit,
    onLeche: (Leche) -> Unit
) {
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
                EmojiCirculo(linea.producto.emoji, 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(linea.producto.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("${linea.unitario.comoPrecio()} c/u", style = MaterialTheme.typography.bodySmall)
                }
                ControlCantidad(linea.cantidad, onSumar, onRestar)
            }

            // Personalización: solo para bebidas
            if (linea.producto.categoria == CATEGORIA_BEBIDAS) {
                SelectorOpciones(
                    titulo = "Tamaño",
                    opciones = Tamano.values().toList(),
                    seleccionada = linea.tamano,
                    etiqueta = { it.etiqueta },
                    onSeleccion = onTamano
                )
                SelectorOpciones(
                    titulo = "Tipo de leche",
                    opciones = Leche.values().toList(),
                    seleccionada = linea.leche,
                    etiqueta = { it.etiqueta },
                    onSeleccion = onLeche
                )
            }

            Text(
                text = "Subtotal: ${linea.subtotal.comoPrecio()}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ---------------------------------------------------------------
// CLIENTE - MIS PEDIDOS (historial guardado en Room)
// ---------------------------------------------------------------
@Composable
fun PantallaMisPedidos(usuario: Usuario, pedidoVm: PedidoViewModel) {
    val flujo = remember(usuario.id) { pedidoVm.historial(usuario.id) }
    val pedidos by flujo.collectAsState(initial = emptyList())

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TituloSeccion("Mis pedidos") }
        if (pedidos.isEmpty()) {
            item {
                Text(
                    "Todavía no has hecho pedidos. Cuando confirmes uno aparecerá aquí.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(pedidos, key = { it.pedido.id }) { item ->
                TarjetaPedido(item = item, mostrarCliente = false)
            }
        }
    }
}
