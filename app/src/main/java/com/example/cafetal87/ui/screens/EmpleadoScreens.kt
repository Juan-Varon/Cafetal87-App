package com.example.cafetal87.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cafetal87.data.CATEGORIAS
import com.example.cafetal87.data.CATEGORIA_BEBIDAS
import com.example.cafetal87.data.Estados
import com.example.cafetal87.data.local.Producto
import com.example.cafetal87.ui.components.EmojiCirculo
import com.example.cafetal87.ui.components.SelectorOpciones
import com.example.cafetal87.ui.components.TarjetaPedido
import com.example.cafetal87.ui.components.TituloSeccion
import com.example.cafetal87.ui.components.comoPrecio
import com.example.cafetal87.viewmodel.AdminViewModel

// ---------------------------------------------------------------
// EMPLEADO - PEDIDOS (todos los pedidos, filtro, estadísticas y cambio de estado)
// ---------------------------------------------------------------
@Composable
fun PantallaPedidosEmpleado(vm: AdminViewModel) {
    val pedidos by vm.pedidos.collectAsState()
    val totalVendido by vm.totalVendido.collectAsState()
    var filtro by remember { mutableStateOf<String?>(null) }

    val visibles = pedidos.filter { filtro == null || it.pedido.estado == filtro }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TituloSeccion("Pedidos de los clientes") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Pedidos", style = MaterialTheme.typography.bodySmall)
                        Text("${pedidos.size}", style = MaterialTheme.typography.titleLarge)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total vendido", style = MaterialTheme.typography.bodySmall)
                        Text(totalVendido.comoPrecio(), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        item {
            SelectorOpciones<String?>(
                titulo = "Filtrar por estado",
                opciones = listOf(null) + Estados.todos,
                seleccionada = filtro,
                etiqueta = { it ?: "Todos" },
                onSeleccion = { filtro = it }
            )
        }
        if (visibles.isEmpty()) {
            item { Text("No hay pedidos para mostrar.", style = MaterialTheme.typography.bodyLarge) }
        } else {
            items(visibles, key = { it.pedido.id }) { item ->
                TarjetaPedido(
                    item = item,
                    mostrarCliente = true,
                    onCambiarEstado = { estado -> vm.cambiarEstadoPedido(item.pedido.id, estado) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// EMPLEADO - PRODUCTOS (consultar, editar, eliminar y cambiar disponibilidad)
// ---------------------------------------------------------------
@Composable
fun PantallaProductosAdmin(
    vm: AdminViewModel,
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit
) {
    val productos by vm.productos.collectAsState()
    var porEliminar by remember { mutableStateOf<Producto?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TituloSeccion("Administrar productos") }
        item {
            Button(onClick = onNuevo, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar producto")
            }
        }
        if (productos.isEmpty()) {
            item { Text("No hay productos registrados.", style = MaterialTheme.typography.bodyLarge) }
        } else {
            items(productos, key = { it.id }) { producto ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EmojiCirculo(producto.emoji, 48.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${producto.categoria} · ${producto.precio.comoPrecio()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    if (producto.disponible) "Disponible" else "Agotado",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (producto.disponible) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                            Switch(
                                checked = producto.disponible,
                                onCheckedChange = { vm.alternarDisponible(producto) }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onEditar(producto.id) }) { Text("Editar") }
                            TextButton(onClick = { porEliminar = producto }) {
                                Text("Eliminar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    val aEliminar = porEliminar
    if (aEliminar != null) {
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que quieres eliminar \"${aEliminar.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.eliminarProducto(aEliminar)
                    porEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

// ---------------------------------------------------------------
// EMPLEADO - FORMULARIO DE PRODUCTO (sirve para crear y para editar)
// ---------------------------------------------------------------
@Composable
fun PantallaFormularioProducto(
    vm: AdminViewModel,
    productoId: Int,
    onTerminar: () -> Unit
) {
    val productos by vm.productos.collectAsState()
    val esEdicion = productoId != 0
    val existente = productos.firstOrNull { it.id == productoId }

    var nombre by remember(existente) { mutableStateOf(existente?.nombre ?: "") }
    var descripcion by remember(existente) { mutableStateOf(existente?.descripcion ?: "") }
    var precio by remember(existente) { mutableStateOf(existente?.precio?.toString() ?: "") }
    var emoji by remember(existente) { mutableStateOf(existente?.emoji ?: "☕") }
    var categoria by remember(existente) { mutableStateOf(existente?.categoria ?: CATEGORIA_BEBIDAS) }
    var disponible by remember(existente) { mutableStateOf(existente?.disponible ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TituloSeccion(if (esEdicion) "Editar producto" else "Nuevo producto")

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; error = null },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it; error = null },
            label = { Text("Precio (solo números)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = emoji,
            onValueChange = { emoji = it },
            label = { Text("Emoji del producto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        SelectorOpciones(
            titulo = "Categoría",
            opciones = CATEGORIAS,
            seleccionada = categoria,
            etiqueta = { it },
            onSeleccion = { categoria = it }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Disponible", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = disponible, onCheckedChange = { disponible = it })
        }

        val mensaje = error
        if (mensaje != null) {
            Text(mensaje, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                error = vm.guardarProducto(
                    existente = existente,
                    nombre = nombre,
                    descripcion = descripcion,
                    precioTexto = precio,
                    categoria = categoria,
                    emoji = emoji,
                    disponible = disponible,
                    alGuardar = onTerminar
                )
            },
            enabled = !esEdicion || existente != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (esEdicion) "Guardar cambios" else "Registrar producto")
        }
        OutlinedButton(onClick = onTerminar, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}
