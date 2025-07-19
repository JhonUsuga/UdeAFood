package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Product
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.components.StoreProductCard
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun StoreProductListScreen(storeId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }

    LaunchedEffect(storeId) {
        db.collection("stores")
            .document(storeId)
            .collection("products")
            .get()
            .addOnSuccessListener { result ->
                products = result.documents.mapNotNull { it.toObject(Product::class.java) }
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Productos disponibles", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(products) { product ->
                StoreProductCard(product = product, storeId = storeId, navController = navController)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

