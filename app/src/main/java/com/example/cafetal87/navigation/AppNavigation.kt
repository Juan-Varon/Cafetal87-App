package com.example.cafetal87.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cafetal87.data.CafeteriaRepository
import com.example.cafetal87.data.Roles
import com.example.cafetal87.data.local.Usuario
import com.example.cafetal87.ui.screens.PantallaFormularioProducto
import com.example.cafetal87.ui.screens.PantallaInicio
import com.example.cafetal87.ui.screens.PantallaLogin
import com.example.cafetal87.ui.screens.PantallaMenu
import com.example.cafetal87.ui.screens.PantallaMisPedidos
import com.example.cafetal87.ui.screens.PantallaPedido
import com.example.cafetal87.ui.screens.PantallaPedidosEmpleado
import com.example.cafetal87.ui.screens.PantallaProductosAdmin
import com.example.cafetal87.ui.screens.PantallaRegistro
import com.example.cafetal87.viewmodel.AdminViewModel
import com.example.cafetal87.viewmodel.AppViewModelFactory
import com.example.cafetal87.viewmodel.AuthViewModel
import com.example.cafetal87.viewmodel.MenuViewModel
import com.example.cafetal87.viewmodel.PedidoViewModel

// Cada destino de la barra de navegación inferior
data class DestinoNav(val ruta: String, val titulo: String, val icono: ImageVector)

private val destinosCliente = listOf(
    DestinoNav("inicio", "Inicio", Icons.Default.Home),
    DestinoNav("menu", "Menú", Icons.Default.Menu),
    DestinoNav("pedido", "Mi pedido", Icons.Default.ShoppingCart),
    DestinoNav("mis_pedidos", "Mis pedidos", Icons.Default.DateRange)
)

private val destinosEmpleado = listOf(
    DestinoNav("pedidos", "Pedidos", Icons.Default.DateRange),
    DestinoNav("productos", "Productos", Icons.Default.Edit)
)

// Punto de entrada: si no hay sesión muestra login/registro; si hay sesión, la app según el rol
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val factory = remember { AppViewModelFactory(CafeteriaRepository.obtener(context)) }
    val authVm: AuthViewModel = viewModel(factory = factory)
    val usuario by authVm.usuario.collectAsState()

    val activo = usuario
    if (activo == null) {
        AuthNavigation(authVm)
    } else {
        PrincipalNavigation(activo, authVm, factory)
    }
}

// Navegación de acceso: login y registro
@Composable
private fun AuthNavigation(authVm: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            PantallaLogin(authVm, onIrARegistro = { navController.navigate("registro") })
        }
        composable("registro") {
            PantallaRegistro(authVm, onVolver = { navController.popBackStack() })
        }
    }
}

// Navegación principal: cambia según el rol del usuario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrincipalNavigation(usuario: Usuario, authVm: AuthViewModel, factory: AppViewModelFactory) {
    val esEmpleado = usuario.rol == Roles.EMPLEADO
    val navController = rememberNavController()

    val menuVm: MenuViewModel = viewModel(factory = factory)
    val pedidoVm: PedidoViewModel = viewModel(factory = factory)
    val adminVm: AdminViewModel = viewModel(factory = factory)

    // Los favoritos pertenecen al usuario que inició sesión
    LaunchedEffect(usuario.id) { menuVm.establecerUsuario(usuario.id) }

    val totalItems by pedidoVm.totalItems.collectAsState()
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    val destinos = if (esEmpleado) destinosEmpleado else destinosCliente
    val rutaInicial = destinos.first().ruta

    val irA: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(rutaInicial)
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text("Cafetal87 ☕", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${usuario.nombre} · ${if (esEmpleado) "Empleado" else "Cliente"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        pedidoVm.vaciar()
                        menuVm.establecerUsuario(null)
                        authVm.cerrarSesion()
                    }) {
                        Text("Salir", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                destinos.forEach { destino ->
                    val seleccionado = rutaActual == destino.ruta ||
                        (destino.ruta == "productos" && rutaActual?.startsWith("producto_form") == true)
                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = { irA(destino.ruta) },
                        icon = {
                            if (destino.ruta == "pedido" && totalItems > 0) {
                                BadgedBox(badge = { Badge { Text("$totalItems") } }) {
                                    Icon(destino.icono, contentDescription = null)
                                }
                            } else {
                                Icon(destino.icono, contentDescription = null)
                            }
                        },
                        label = { Text(destino.titulo) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = rutaInicial,
            modifier = Modifier.padding(padding)
        ) {
            // ----- Cliente -----
            composable("inicio") {
                PantallaInicio(usuario, menuVm, pedidoVm, onIrAlMenu = { irA("menu") })
            }
            composable("menu") {
                PantallaMenu(menuVm, pedidoVm)
            }
            composable("pedido") {
                PantallaPedido(
                    usuario = usuario,
                    pedidoVm = pedidoVm,
                    onIrAlMenu = { irA("menu") },
                    onIrAMisPedidos = { irA("mis_pedidos") }
                )
            }
            composable("mis_pedidos") {
                PantallaMisPedidos(usuario, pedidoVm)
            }
            // ----- Empleado -----
            composable("pedidos") {
                PantallaPedidosEmpleado(adminVm)
            }
            composable("productos") {
                PantallaProductosAdmin(
                    vm = adminVm,
                    onNuevo = { navController.navigate("producto_form/0") },
                    onEditar = { id -> navController.navigate("producto_form/$id") }
                )
            }
            composable(
                route = "producto_form/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                PantallaFormularioProducto(
                    vm = adminVm,
                    productoId = id,
                    onTerminar = { navController.popBackStack() }
                )
            }
        }
    }
}
