package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Store
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavController

@Composable
fun StoreDashboardScreen(storeId: String, navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var store by remember { mutableStateOf<Store?>(null) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            db.collection("stores").document(uid).get().addOnSuccessListener { doc ->
                store = doc.toObject(Store::class.java)
            }

            db.collection("stores").document(uid).collection("products")
                .get()
                .addOnSuccessListener { result ->
                    products = result.documents.mapNotNull { it.toObject(Product::class.java) }
                }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Panel de Tienda", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        store?.let {
            Text("Nombre: ${it.name}")
            Text("Descripción: ${it.description}")
            Text("Horario: ${it.openHour} - ${it.closeHour}")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            navController.navigate("edit_store/$storeId")
        }) {
            Text("Editar información")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Productos", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(products) { product ->
                Text("- ${product.name}: ${product.price}")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            navController.navigate("add_product/${storeId}")
        }) {
            Text("Agregar nuevo producto")
        }

        Button(
            onClick = { navController.navigate("store_reviews") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver reseñas de productos")
        }

        OutlinedButton(
            onClick = { navController.navigate("map") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver el mapa")
        }

    }
}
