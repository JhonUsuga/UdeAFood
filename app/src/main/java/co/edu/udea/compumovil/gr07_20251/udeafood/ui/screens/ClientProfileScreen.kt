package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.theme.GreenActive
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(clientId: String, navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("map") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Nombre: ${user?.displayName ?: "Sin nombre"}", style = MaterialTheme.typography.bodyLarge)
            Text("Correo: ${user?.email ?: "Sin correo"}", style = MaterialTheme.typography.bodyLarge)

            Button(
                onClick = {
                    // lógica para cambio de contraseña, por ahora puede ser solo un Toast o mensaje
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cambiar contraseña", color = MaterialTheme.colorScheme.onPrimary)
            }

            Button(
                onClick = {
                    navController.navigate("favorites")
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mis productos favoritos", color = MaterialTheme.colorScheme.onPrimary)
            }

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("auth_choice") {
                        popUpTo("map") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.onError)
            }

            OutlinedButton(
                onClick = { navController.navigate("map") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al mapa")
            }
        }
    }
}
