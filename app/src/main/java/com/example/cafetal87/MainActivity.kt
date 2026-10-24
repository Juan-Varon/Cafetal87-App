package com.example.cafetal87

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cafetal87.ui.theme.Cafetal87Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cafetal87Theme {
                Cafetal87App()
            }
        }
    }
}

// Cada destino de la barra de navegación inferior
data class DestinoNav(val ruta: String, val titulo: String, val icono: ImageVector)

val destinos = listOf(
    DestinoNav("inicio", "Inicio", Icons.Default.Home),
    DestinoNav("menu", "Menú", Icons.Default.Menu),
    DestinoNav("pedido", "Mi pedido", Icons.Default.ShoppingCart)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cafetal87App() {
    // Navigation Compose: el NavController controla a qué pantalla vamos
    val navController = rememberNavController()
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    // ESTADO compartido entre pantallas (vive aquí, arriba de todo)
    val cantidades = remember { mutableStateMapOf<Int, Int>() }
    val favoritosIds = remember { mutableStateListOf<Int>() }
    val tamanos = remember { mutableStateMapOf<Int, Tamano>() }
    val leches = remember { mutableStateMapOf<Int, Leche>() }

    // EVENTOS: funciones que cambian el estado
    val sumar: (Producto) -> Unit = { p ->
        cantidades[p.id] = (cantidades[p.id] ?: 0) + 1
    }
    val restar: (Producto) -> Unit = { p ->
        val actual = cantidades[p.id] ?: 0
        if (actual > 0) cantidades[p.id] = actual - 1
    }
    val alternarFavorito: (Producto) -> Unit = { p ->
        if (p.id in favoritosIds) favoritosIds.remove(p.id) else favoritosIds.add(p.id)
    }
    val vaciarPedido: () -> Unit = {
        cantidades.clear()
        tamanos.clear()
        leches.clear()
    }

    val totalItems = cantidades.values.sum()
    val favoritos = listaProductos.filter { it.id in favoritosIds }

    val irA: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo("inicio")
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cafetal87 ☕", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                destinos.forEach { destino ->
                    NavigationBarItem(
                        selected = rutaActual == destino.ruta,
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
        // NavHost: define las pantallas y la pantalla inicial
        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier.padding(padding)
        ) {
            composable("inicio") {
                PantallaInicio(
                    favoritos = favoritos,
                    cantidades = cantidades,
                    onSumar = sumar,
                    onRestar = restar,
                    onFavorito = alternarFavorito,
                    onIrAlMenu = { irA("menu") }
                )
            }
            composable("menu") {
                PantallaMenu(
                    favoritosIds = favoritosIds,
                    cantidades = cantidades,
                    onSumar = sumar,
                    onRestar = restar,
                    onFavorito = alternarFavorito
                )
            }
            composable("pedido") {
                PantallaPedido(
                    cantidades = cantidades,
                    tamanos = tamanos,
                    leches = leches,
                    onSumar = sumar,
                    onRestar = restar,
                    onTamano = { p, t -> tamanos[p.id] = t },
                    onLeche = { p, l -> leches[p.id] = l },
                    onConfirmar = vaciarPedido,
                    onIrAlMenu = { irA("menu") }
                )
            }
        }
    }
}