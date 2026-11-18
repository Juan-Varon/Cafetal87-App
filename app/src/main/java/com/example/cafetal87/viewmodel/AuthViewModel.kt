package com.example.cafetal87.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cafetal87.data.CODIGO_EMPLEADO
import com.example.cafetal87.data.CafeteriaRepository
import com.example.cafetal87.data.Roles
import com.example.cafetal87.data.hashContrasena
import com.example.cafetal87.data.local.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Maneja el registro, el inicio de sesión y el usuario activo
class AuthViewModel(private val repo: CafeteriaRepository) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun limpiarError() {
        _error.value = null
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        val correoLimpio = correo.trim().lowercase()
        if (correoLimpio.isEmpty() || contrasena.isEmpty()) {
            _error.value = "Ingresa tu correo y tu contraseña"
            return
        }
        viewModelScope.launch {
            val encontrado = repo.buscarUsuario(correoLimpio)
            if (encontrado == null || encontrado.contrasenaHash != hashContrasena(contrasena)) {
                _error.value = "Correo o contraseña incorrectos"
            } else {
                _error.value = null
                _usuario.value = encontrado
            }
        }
    }

    fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        confirmar: String,
        rol: String,
        codigo: String
    ) {
        val correoLimpio = correo.trim().lowercase()
        val mensaje = when {
            nombre.isBlank() -> "El nombre es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches() -> "Ingresa un correo válido"
            contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            contrasena != confirmar -> "Las contraseñas no coinciden"
            rol == Roles.EMPLEADO && codigo.trim() != CODIGO_EMPLEADO -> "El código de empleado es incorrecto"
            else -> null
        }
        if (mensaje != null) {
            _error.value = mensaje
            return
        }
        viewModelScope.launch {
            if (repo.buscarUsuario(correoLimpio) != null) {
                _error.value = "Ese correo ya está registrado"
                return@launch
            }
            repo.registrarUsuario(
                Usuario(
                    nombre = nombre.trim(),
                    correo = correoLimpio,
                    contrasenaHash = hashContrasena(contrasena),
                    rol = rol
                )
            )
            _error.value = null
            // Después de registrarse, inicia sesión automáticamente
            _usuario.value = repo.buscarUsuario(correoLimpio)
        }
    }

    fun cerrarSesion() {
        _usuario.value = null
    }
}
