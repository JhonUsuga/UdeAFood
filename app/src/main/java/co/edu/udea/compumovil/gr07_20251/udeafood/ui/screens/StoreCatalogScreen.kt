package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Product
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.components.ProductCard
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun StoreCatalogScreen(storeId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(storeId) {
        try {
            val snapshot = db.collection("stores")
                .document(storeId)
                .collection("products")
                .get()
                .await()

            products = snapshot.documents.mapNotNull {
                it.toObject(Product::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoading = false
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Productos de la tienda", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (products.isEmpty()) {
            Text("Esta tienda no tiene productos aún.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(products) { product ->
                    ProductCard(product = product, navController = navController)
                }
            }
        }
    }
}
