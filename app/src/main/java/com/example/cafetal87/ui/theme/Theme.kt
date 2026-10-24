package com.example.cafetal87.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de colores Material 3 personalizado para Cafetal87
private val EsquemaCafe = lightColorScheme(
    primary = CafeOscuro,
    onPrimary = Color.White,
    primaryContainer = CafeLeche,
    onPrimaryContainer = CafeOscuro,
    secondary = Caramelo,
    onSecondary = TextoOscuro,
    secondaryContainer = CremaOscura,
    onSecondaryContainer = CafeOscuro,
    tertiary = Terracota,
    onTertiary = Color.White,
    background = CremaClara,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = CremaOscura,
    onSurfaceVariant = CafeOscuro
)

@Composable
fun Cafetal87Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaCafe,
        typography = Typography,
        content = content
    )
}