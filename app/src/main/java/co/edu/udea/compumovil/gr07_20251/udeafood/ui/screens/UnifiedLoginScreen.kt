package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.theme.GreenActive
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.compose.ui.res.painterResource
import co.edu.udea.compumovil.gr07_20251.udeafood.R
import kotlinx.coroutines.launch

@Composable
fun UnifiedLoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedType by remember { mutableStateOf<String?>(null) } // "client" o "store"

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            firebaseAuthWithGoogle(account, auth, db, selectedType, context, navController)
        } else {
            Toast.makeText(context, "Fallo al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
        }
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("TU_CLIENT_ID_WEB") // <- Reemplaza por tu client ID web de Firebase
        .requestEmail()
        .build()
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F7FD))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a UdeaFood", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Selecciona tu tipo de cuenta")
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { selectedType = "store" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == "store") GreenActive else Color.LightGray
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Tienda")
            }

            Button(
                onClick = { selectedType = "client" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == "client") GreenActive else Color.LightGray
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cliente")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (selectedType != null) {
                    val signInIntent: Intent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                } else {
                    Toast.makeText(context, "Selecciona el tipo de cuenta", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Continuar con Google", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (selectedType != null){
                    navController.navigate("email_login/$selectedType")
                }else{
                    Toast.makeText(context, "Selecciona el tipo de cuenta", Toast.LENGTH_SHORT).show()
                }},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Email, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar con correo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                if (selectedType.isNullOrEmpty()) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Por favor selecciona un tipo de cuenta primero.")
                    }
                } else {
                    val route = if (selectedType == "client") "client_register" else "store_register"
                    navController.navigate(route)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿No tienes cuenta? Regístrate", color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun firebaseAuthWithGoogle(
    acct: GoogleSignInAccount?,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    type: String?,
    context: android.content.Context,
    navController: NavController
) {
    val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
    auth.signInWithCredential(credential)
        .addOnSuccessListener { result ->
            val uid = result.user?.uid ?: return@addOnSuccessListener
            val name = result.user?.displayName ?: ""
            val email = result.user?.email ?: ""

            val baseData = mapOf("id" to uid, "name" to name, "email" to email, "type" to type)

            val collection = if (type == "store") "stores" else "users"

            db.collection(collection).document(uid).get().addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    db.collection(collection).document(uid).set(baseData)
                }
                Toast.makeText(context, "Inicio exitoso", Toast.LENGTH_SHORT).show()
                // Redirección condicional
                if (type == "store") {
                    navController.navigate("store_dashboard")
                } else {
                    navController.navigate("catalog")
                }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al autenticar: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}