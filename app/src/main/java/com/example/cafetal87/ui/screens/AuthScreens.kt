package com.example.cafetal87.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafetal87.data.Roles
import com.example.cafetal87.ui.components.SelectorOpciones
import com.example.cafetal87.viewmodel.AuthViewModel

// ---------------------------------------------------------------
// INICIO DE SESIÓN
// ---------------------------------------------------------------
@Composable
fun PantallaLogin(vm: AuthViewModel, onIrARegistro: () -> Unit) {
    val error by vm.error.collectAsState()
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.limpiarError() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("☕", fontSize = 64.sp)
            Text(
                "Cafetal87",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Inicia sesión para continuar", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it; vm.limpiarError() },
                label = { Text("Correo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it; vm.limpiarError() },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            val mensaje = error
            if (mensaje != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { vm.iniciarSesion(correo, contrasena) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }
            TextButton(onClick = onIrARegistro) {
                Text("¿No tienes cuenta? Regístrate")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Cuentas de prueba", style = MaterialTheme.typography.labelLarge)
                    Text("Empleado: empleado@cafetal87.com / empleado123", style = MaterialTheme.typography.bodySmall)
                    Text("Cliente: cliente@cafetal87.com / cliente123", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// REGISTRO DE USUARIO (cliente o empleado)
// ---------------------------------------------------------------
@Composable
fun PantallaRegistro(vm: AuthViewModel, onVolver: () -> Unit) {
    val error by vm.error.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(Roles.CLIENTE) }
    var codigo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.limpiarError() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Crear cuenta",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Regístrate como cliente o como empleado de Cafetal87", style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; vm.limpiarError() },
                label = { Text("Nombre completo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it; vm.limpiarError() },
                label = { Text("Correo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it; vm.limpiarError() },
                label = { Text("Contraseña (mínimo 6 caracteres)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmar,
                onValueChange = { confirmar = it; vm.limpiarError() },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            SelectorOpciones(
                titulo = "Tipo de cuenta",
                opciones = listOf(Roles.CLIENTE, Roles.EMPLEADO),
                seleccionada = rol,
                etiqueta = { if (it == Roles.CLIENTE) "Cliente" else "Empleado" },
                onSeleccion = { rol = it; vm.limpiarError() }
            )

            if (rol == Roles.EMPLEADO) {
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it; vm.limpiarError() },
                    label = { Text("Código de empleado") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val mensaje = error
            if (mensaje != null) {
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { vm.registrar(nombre, correo, contrasena, confirmar, rol, codigo) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cuenta")
            }
            TextButton(onClick = onVolver) {
                Text("Ya tengo cuenta, volver")
            }
        }
    }
}
