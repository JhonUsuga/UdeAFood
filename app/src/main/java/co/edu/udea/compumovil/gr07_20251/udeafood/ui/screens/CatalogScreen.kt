package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Store
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.components.RestaurantCard
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.components.TopBar
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CatalogScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var stores by remember { mutableStateOf<List<Store>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("stores").get()
            .addOnSuccessListener { result ->
                stores = result.documents.mapNotNull { it.toObject(Store::class.java) }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title = "UdeaFood")

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text("All restaurants", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(stores) { store ->
                RestaurantCard(store = store) {
                    navController.navigate("store_products/${store.id}")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}