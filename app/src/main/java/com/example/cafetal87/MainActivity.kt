package com.example.cafetal87

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.cafetal87.data.CafeteriaRepository
import com.example.cafetal87.navigation.AppNavigation
import com.example.cafetal87.ui.theme.Cafetal87Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga los usuarios y productos iniciales en Room (solo si la base está vacía)
        val repo = CafeteriaRepository.obtener(applicationContext)
        lifecycleScope.launch { repo.sembrarDatosIniciales() }

        setContent {
            Cafetal87Theme {
                AppNavigation()
            }
        }
    }
}
